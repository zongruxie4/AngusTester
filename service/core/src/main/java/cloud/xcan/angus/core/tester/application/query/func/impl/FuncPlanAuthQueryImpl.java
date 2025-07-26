package cloud.xcan.angus.core.tester.application.query.func.impl;

import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.FUNC_PLAN_NO_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.hasPolicy;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isTenantSysAdmin;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.TesterConstant;
import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanQuery;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Implementation of FuncPlanAuthQuery for managing functional test plan authorization queries.
 * <p>
 * This class provides comprehensive functionality for querying, validating, and managing
 * authorization permissions for functional test plans. It handles user permissions, admin checks,
 * creator validation, and batch permission operations.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Authorization status checking and validation</li>
 *   <li>User permission retrieval and verification</li>
 *   <li>Admin and creator privilege management</li>
 *   <li>Batch permission checking for multiple plans</li>
 *   <li>Authorization conflict detection and prevention</li>
 * </ul>
 * <p>
 * The implementation uses BizTemplate pattern for consistent business logic execution and
 * includes performance optimizations for bulk operations and permission checks.
 * <p>
 * Supports both individual plan operations and bulk operations with proper error handling
 * and resource validation.
 */
@Biz
public class FuncPlanAuthQueryImpl implements FuncPlanAuthQuery {

  @Resource
  private FuncPlanAuthRepo funcPlanAuthRepo;
  @Resource
  private FuncPlanQuery funcPlanQuery;
  @Resource
  private FuncPlanRepo funcPlanRepo;
  @Resource
  private UserRepo userRepo;

  /**
   * Retrieves the authorization status for a functional test plan.
   * <p>
   * Checks if authorization control is enabled for the specified plan.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to check authorization status for
   * @return Boolean indicating whether authorization control is enabled
   * @throws ResourceNotFound if the plan is not found
   */
  @Override
  public Boolean status(Long planId) {
    return new BizTemplate<Boolean>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Validate that the functional test plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected Boolean process() {
        return funcPlanDb.getAuth();
      }
    }.execute();
  }

  /**
   * Retrieves user permissions for a specific functional test plan.
   * <p>
   * Provides comprehensive permission analysis including admin privileges, creator rights,
   * and assigned permissions. Supports admin override functionality.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to check permissions for
   * @param userId the user ID to retrieve permissions for
   * @param admin whether to include admin privileges in the check
   * @return List of FuncPlanPermission objects representing user permissions
   * @throws ResourceNotFound if the plan is not found
   */
  @Override
  public List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean admin) {
    return new BizTemplate<List<FuncPlanPermission>>() {
      @Override
      protected void checkParams() {
        // Validate that the functional test plan exists
        funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected List<FuncPlanPermission> process() {
        // Grant all permissions if admin override is enabled and user is admin
        if (Objects.nonNull(admin) && admin && isAdminUser()) {
          return FuncPlanPermission.ALL;
        }

        // Retrieve user's authorization records for the plan
        List<FuncPlanAuth> auths = findAuth(userId, planId);
        
        // Grant all permissions if user is the creator
        if (isCreator(auths)) {
          return FuncPlanPermission.ALL;
        }

        // Extract and return distinct permissions from authorization records
        return auths.stream()
            .map(FuncPlanAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
      }
    }.execute();
  }

  /**
   * Retrieves current user's authorization status and permissions for a functional test plan.
   * <p>
   * Provides comprehensive authorization information including plan authorization status,
   * user permissions, and admin/creator privileges for the current authenticated user.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to check current user permissions for
   * @param admin whether to include admin privileges in the check
   * @return FuncPlanAuthCurrent object with authorization status and permissions
   * @throws ResourceNotFound if the plan is not found
   */
  @Override
  public FuncPlanAuthCurrent currentUserAuth(Long planId, Boolean admin) {
    return new BizTemplate<FuncPlanAuthCurrent>() {
      FuncPlan funcPlanDb;

      @Override
      protected void checkParams() {
        // Validate that the functional test plan exists
        funcPlanDb = funcPlanQuery.checkAndFind(planId);
      }

      @Override
      protected FuncPlanAuthCurrent process() {
        FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
        authCurrent.setFuncPlanAuth(funcPlanDb.getAuth());

        // Grant all permissions if admin override is enabled and current user is admin
        if (Objects.nonNull(admin) && admin && isAdminUser()) {
          authCurrent.addPermissions(FuncPlanPermission.ALL);
          return authCurrent;
        }

        // Retrieve current user's authorization records for the plan
        List<FuncPlanAuth> auths = findAuth(getUserId(), planId);
        
        // Grant all permissions if current user is the creator
        if (isCreator(auths)) {
          authCurrent.addPermissions(FuncPlanPermission.ALL);
          return authCurrent;
        }

        // Extract and add distinct permissions from authorization records
        List<FuncPlanPermission> authPermissions = auths.stream()
            .map(FuncPlanAuth::getAuths).flatMap(Collection::stream).distinct()
            .collect(Collectors.toList());
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  /**
   * Retrieves current user's authorization status and permissions for multiple functional test plans.
   * <p>
   * Provides batch authorization information for multiple plans with optimized database queries.
   * Handles admin privileges, creator rights, and assigned permissions efficiently.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planIds set of plan IDs to check current user permissions for
   * @param admin whether to include admin privileges in the check
   * @return Map of plan ID to FuncPlanAuthCurrent objects with authorization status and permissions
   * @throws ResourceNotFound if any plan is not found
   */
  @Override
  public Map<Long, FuncPlanAuthCurrent> currentUserAuths(HashSet<Long> planIds, Boolean admin) {
    return new BizTemplate<Map<Long, FuncPlanAuthCurrent>>() {
      List<FuncPlan> planDb;

      @Override
      protected void checkParams() {
        // Validate that all functional test plans exist
        planDb = funcPlanQuery.checkAndFind(planIds);
      }

      @Override
      protected Map<Long, FuncPlanAuthCurrent> process() {
        Map<Long, FuncPlanAuthCurrent> authCurrentMap = new HashMap<>();
        
        // Grant all permissions for all plans if admin override is enabled and current user is admin
        if (nonNull(admin) && admin && isAdminUser()) {
          for (FuncPlan plan : planDb) {
            FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
            authCurrent.setFuncPlanAuth(plan.getAuth());
            authCurrent.addPermissions(FuncPlanPermission.ALL);
            authCurrentMap.put(plan.getId(), authCurrent);
          }
          return authCurrentMap;
        }

        // Identify plans where current user is the creator
        Set<Long> currentCreatorIds = planDb.stream()
            .filter(x -> x.getCreatedBy().equals(getUserId())).map(FuncPlan::getId)
            .collect(Collectors.toSet());
            
        // Grant all permissions for plans where current user is creator
        if (isNotEmpty(currentCreatorIds)) {
          for (FuncPlan plan : planDb) {
            if (currentCreatorIds.contains(plan.getId())) {
              FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
              authCurrent.setFuncPlanAuth(plan.getAuth());
              authCurrent.addPermissions(FuncPlanPermission.ALL);
              authCurrentMap.put(plan.getId(), authCurrent);
            }
          }
        }

        // Process remaining plans that require authorization lookup
        Set<Long> remainIds = new HashSet<>(planIds);
        remainIds.removeAll(currentCreatorIds);
        if (isNotEmpty(remainIds)) {
          // Batch retrieve authorization records for remaining plans
          Map<Long, List<FuncPlanAuth>> planAuthsMap = findAuth(getUserId(), remainIds)
              .stream().collect(Collectors.groupingBy(FuncPlanAuth::getPlanId));
              
          for (FuncPlan plan : planDb) {
            if (remainIds.contains(plan.getId())) {
              FuncPlanAuthCurrent authCurrent = new FuncPlanAuthCurrent();
              Set<FuncPlanPermission> permissions = new HashSet<>();
              List<FuncPlanAuth> planAuths = planAuthsMap.get(plan.getId());
              if (isNotEmpty(planAuths)) {
                Set<FuncPlanPermission> authPermissions = planAuths.stream()
                    .map(FuncPlanAuth::getAuths).flatMap(Collection::stream)
                    .collect(Collectors.toSet());
                permissions.addAll(authPermissions);
              }
              authCurrent.addPermissions(permissions);
              authCurrent.setFuncPlanAuth(plan.getAuth());
              authCurrentMap.put(plan.getId(), authCurrent);
            }
          }
        }
        return authCurrentMap;
      }
    }.execute();
  }

  /**
   * Validates user permission for a specific functional test plan.
   * <p>
   * Checks if the specified user has the required permission for the plan.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param planId the plan ID to check permission for
   * @param permission the permission to validate
   * @param userId the user ID to check permission for
   * @throws BizException if user lacks the required permission
   */
  @Override
  public void check(Long planId, FuncPlanPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, planId, permission);
        return null;
      }
    }.execute();
  }

  /**
   * Retrieves paginated authorization records for functional test plans.
   * <p>
   * Provides filtered and paginated access to authorization records with specification-based
   * filtering and plan ID validation.
   * <p>
   * Uses BizTemplate pattern for consistent business logic execution.
   *
   * @param spec the specification for filtering authorization records
   * @param planIds list of plan IDs to filter by (required)
   * @param pageable pagination parameters
   * @return Page of FuncPlanAuth objects
   * @throws ProtocolException if planIds parameter is missing
   */
  @Override
  public Page<FuncPlanAuth> find(Specification<FuncPlanAuth> spec, List<String> planIds,
      Pageable pageable) {
    return new BizTemplate<Page<FuncPlanAuth>>() {
      @Override
      protected void checkParams() {
        if (isEmpty(planIds)) {
          throw ProtocolException.of(PARAM_MISSING_T, PARAM_MISSING_KEY, new Object[]{"planId"});
        }
      }

      @Override
      protected Page<FuncPlanAuth> process() {
        return funcPlanAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * Finds an authorization record by ID with validation.
   * <p>
   * Retrieves an authorization record and throws ResourceNotFound if not found.
   *
   * @param id the authorization record ID
   * @return FuncPlanAuth object
   * @throws ResourceNotFound if authorization record is not found
   */
  @Override
  public FuncPlanAuth checkAndFind(Long id) {
    return funcPlanAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "FuncPlanAuth"));
  }

  /**
   * Validates user permission to modify a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks modify plan permission
   */
  @Override
  public void checkModifyPlanAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.MODIFY_PLAN);
  }

  /**
   * Validates user permission to delete a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks delete plan permission
   */
  @Override
  public void checkDeletePlanAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.DELETE_PLAN);
  }

  /**
   * Validates user permission to add cases to a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks add case permission
   */
  @Override
  public void checkAddCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.ADD_CASE);
  }

  /**
   * Validates user permission to modify cases in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks modify case permission
   */
  @Override
  public void checkModifyCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.MODIFY_CASE);
  }

  /**
   * Validates user permission to export cases from a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks export case permission
   */
  @Override
  public void checkExportCaseAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.EXPORT_CASE);
  }

  /**
   * Validates user permission to review cases in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks review permission
   */
  @Override
  public void checkReviewAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.REVIEW);
  }

  /**
   * Validates user permission to reset review results in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks reset review result permission
   */
  @Override
  public void checkResetReviewResultAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.RESET_REVIEW_RESULT);
  }

  /**
   * Validates user permission to test cases in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks test permission
   */
  @Override
  public void checkTestAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.TEST);
  }

  /**
   * Validates user permission to reset test results in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks reset test result permission
   */
  @Override
  public void checkResetTestResultAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.RESET_TEST_RESULT);
  }

  /**
   * Validates user permission to establish baselines in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks establish baseline permission
   */
  @Override
  public void checkEstablishBaselineAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.ESTABLISH_BASELINE);
  }

  /**
   * Validates user permission to grant authorization in a functional test plan.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @throws BizException if user lacks grant permission
   */
  @Override
  public void checkGrantAuth(Long userId, Long planId) {
    checkAuth(userId, planId, FuncPlanPermission.GRANT, false, true);
  }

  /**
   * Validates user permission for a functional test plan with default settings.
   * <p>
   * Checks if the specified user has the required permission for the plan using
   * default admin and public access handling.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @param permission the permission to validate
   * @throws BizException if user lacks the required permission
   */
  @Override
  public void checkAuth(Long userId, Long planId, FuncPlanPermission permission) {
    checkAuth(userId, planId, permission, false, permission.isGrant());
  }

  /**
   * Validates user permission for a functional test plan with custom settings.
   * <p>
   * Provides comprehensive permission validation with configurable admin and public access handling.
   * Supports special cases for grant permissions and authorization control bypass.
   *
   * @param userId the user ID to check permission for
   * @param planId the plan ID to check permission for
   * @param permission the permission to validate
   * @param ignoreAdminPermission whether to ignore admin privilege checks
   * @param ignorePublicAccess whether to ignore public access checks
   * @throws BizException if user lacks the required permission
   */
  @Override
  public void checkAuth(Long userId, Long planId, FuncPlanPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    // Skip validation for admin users or non-user actions unless explicitly ignored
    if (!ignoreAdminPermission && isAdminUser() || !isUserAction()) {
      return;
    }

    // Skip validation for public access permissions when authorization control is disabled
    // This prevents users without authorization permissions from granting permissions
    if (!ignorePublicAccess && !permission.notIgnorePublicAccess()
        && !funcPlanQuery.isAuthCtrl(planId)) {
      return;
    }

    // Retrieve user's authorization records for the plan
    List<FuncPlanAuth> planAuths = findAuth(userId, planId);

    // Grant access if user is the creator
    if (isCreator(planAuths)) {
      return;
    }
    
    // Validate that user has the required permission
    if (!findDirAction(planAuths).contains(permission)) {
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * Validates operation permissions for multiple functional test plans in batch.
   * <p>
   * Provides efficient batch permission checking for multiple plans with optimized
   * database queries and comprehensive error reporting.
   * <p>
   * Supports grant permission special handling and authorization control filtering.
   *
   * @param planIds collection of plan IDs to check permissions for
   * @param permission the permission to validate across all plans
   * @throws BizException if user lacks the required permission for any plan
   */
  @Override
  public void batchCheckPermission(Collection<Long> planIds, FuncPlanPermission permission) {
    // Skip validation for admin users, empty collections, null permissions, or non-user actions
    if (isAdminUser() || isEmpty(planIds) || Objects.isNull(permission) || !isUserAction()) {
      return;
    }

    // Filter plans based on permission type and authorization control status
    Collection<Long> authIds = permission.isGrant()
        ? planIds : funcPlanRepo.findIds0ByIdInAndAuth(planIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    // Retrieve authorization records for filtered plans
    List<FuncPlanAuth> auths = findAuth(PrincipalContext.getUserId(), authIds);
    if (isEmpty(auths)) {
      // Report first plan as example when no authorization records found
      long firstId = authIds.stream().findFirst().get();
      FuncPlan plan = funcPlanRepo.findById(firstId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(plan) ? firstId : plan.getName()});
    }

    // Group authorization records by plan ID for efficient processing
    Map<Long, List<FuncPlanAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getPlanId()))
        .collect(Collectors.groupingBy(FuncPlanAuth::getPlanId));
        
    // Validate permissions for each plan
    for (Long planId : authMap.keySet()) {
      List<FuncPlanAuth> values = authMap.get(planId);
      if (isNotEmpty(values)) {
        List<FuncPlanPermission> planPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(planPermissions) && planPermissions.contains(permission)) {
          continue;
        }
      }
      // Report plan details when permission validation fails
      FuncPlan plan = funcPlanRepo.findById(planId).orElse(null);
      throw BizException.of(FUNC_PLAN_NO_AUTH_CODE, FUNC_PLAN_NO_AUTH,
          new Object[]{permission, Objects.isNull(plan) ? planId : plan.getName()});
    }
  }

  /**
   * Validates that authorization record does not already exist for the specified parameters.
   * <p>
   * Prevents duplicate authorization records by checking for existing entries with
   * the same plan, object, and type combination.
   *
   * @param planId the plan ID
   * @param authObjectId the authorization object ID
   * @param authObjectType the authorization object type
   * @throws ResourceExisted if authorization record already exists
   */
  @Override
  public void checkRepeatAuth(Long planId, Long authObjectId, AuthObjectType authObjectType) {
    if (funcPlanAuthRepo.countByPlanIdAndAuthObjectIdAndAuthObjectType(planId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  /**
   * Finds plan IDs where user has the specified permission.
   * <p>
   * Searches for plans where the user or their organizations have the required permission.
   * Includes both direct user permissions and organizational permissions.
   *
   * @param userId the user ID to search permissions for
   * @param permission the permission to search for
   * @return List of plan IDs where user has the specified permission
   */
  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, FuncPlanPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return funcPlanAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(FuncPlanAuth::getPlanId).collect(
            Collectors.toList());
  }

  /**
   * Finds authorization records for a specific user and plan.
   * <p>
   * Retrieves all authorization records for the user and their organizations
   * for the specified plan.
   *
   * @param userId the user ID
   * @param planId the plan ID
   * @return List of FuncPlanAuth objects
   */
  @Override
  public List<FuncPlanAuth> findAuth(Long userId, Long planId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return funcPlanAuthRepo.findAllByPlanIdAndAuthObjectIdIn(planId, orgIds);
  }

  /**
   * Finds authorization records for a specific user and multiple plans.
   * <p>
   * Retrieves all authorization records for the user and their organizations
   * for the specified plans. Optimized for bulk operations.
   *
   * @param userId the user ID
   * @param planIds collection of plan IDs
   * @return List of FuncPlanAuth objects
   */
  @Override
  public List<FuncPlanAuth> findAuth(Long userId, Collection<Long> planIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(planIds) ? funcPlanAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : funcPlanAuthRepo.findAllByPlanIdInAndAuthObjectIdIn(planIds, orgIds);
  }

  /**
   * Retrieves user permissions for a specific functional test plan.
   * <p>
   * Provides comprehensive permission analysis including admin privileges and creator rights.
   * Returns null if no permissions are found.
   *
   * @param planId the plan ID to check permissions for
   * @param userId the user ID to retrieve permissions for
   * @return List of FuncPlanPermission objects, or null if no permissions found
   */
  @Override
  public List<FuncPlanPermission> getUserAuth(Long planId, Long userId) {
    if (isAdminUser()) {
      return FuncPlanPermission.ALL;
    }

    List<FuncPlanAuth> planAuths = findAuth(userId, planId);
    if (isEmpty(planAuths)) {
      return null;
    }
    if (isCreator(planAuths)) {
      return FuncPlanPermission.ALL;
    }

    return planAuths.stream().map(FuncPlanAuth::getAuths).flatMap(Collection::stream)
        .distinct().collect(Collectors.toList());
  }

  /**
   * Checks if the current user has administrative privileges.
   * <p>
   * Validates admin status based on policy permissions and tenant system admin status.
   *
   * @return true if user has admin privileges, false otherwise
   */
  @Override
  public boolean isAdminUser() {
    return hasPolicy(TesterConstant.ANGUSTESTER_ADMIN) || isTenantSysAdmin();
  }

  /**
   * Checks if the specified user is the creator of the functional test plan.
   * <p>
   * Validates creator status by checking authorization records for creator flag.
   *
   * @param userId the user ID to check
   * @param planId the plan ID to check
   * @return true if user is the creator, false otherwise
   */
  @Override
  public boolean isCreator(Long userId, Long planId) {
    List<FuncPlanAuth> planAuths = findAuth(userId, planId);
    return isCreator(planAuths);
  }

  /**
   * Checks if any authorization record indicates creator status.
   * <p>
   * Internal helper method to validate creator status from authorization records.
   *
   * @param planAuths list of authorization records to check
   * @return true if any record indicates creator status, false otherwise
   */
  private boolean isCreator(List<FuncPlanAuth> planAuths) {
    if (planAuths.isEmpty()) {
      return false;
    }
    for (FuncPlanAuth auth : planAuths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Extracts all permissions from authorization records.
   * <p>
   * Internal helper method to collect all permissions from a list of authorization records.
   *
   * @param planAuths list of authorization records to extract permissions from
   * @return Set of FuncPlanPermission objects
   */
  private Set<FuncPlanPermission> findDirAction(List<FuncPlanAuth> planAuths) {
    Set<FuncPlanPermission> actions = new HashSet<>();
    for (FuncPlanAuth auth : planAuths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}
