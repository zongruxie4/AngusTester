package cloud.xcan.angus.core.tester.application.query.apis.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfoRepo;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthCurrent;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
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
 * Implementation of API authorization query operations for permission management.
 *
 * <p>This class provides comprehensive functionality for querying and validating
 * API permissions, including user authorization checks, permission validation, and authorization
 * status management.</p>
 *
 * <p>It handles various permission types including view, modify, delete, debug,
 * test, grant, share, release, and export permissions with proper validation.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>User permission validation and checking</li>
 *   <li>Current user authorization status</li>
 *   <li>Batch permission validation for multiple APIs</li>
 *   <li>Creator permission handling</li>
 *   <li>Admin user privilege management</li>
 *   <li>Authorization status queries</li>
 *   <li>Permission flatting and aggregation</li>
 * </ul></p>
 */
@Service
public class ApisAuthQueryImpl implements ApisAuthQuery {

  @Resource
  private ApisAuthRepo apisAuthRepo;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisBaseInfoRepo apisBaseInfoRepo;
  @Resource
  private UserRepo userRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Gets the authorization status for an API.
   *
   * <p>This method retrieves whether authorization is enabled for a specific API,
   * indicating whether permission checks are required for access.</p>
   *
   * @param apiId the API ID to check authorization status for
   * @return true if authorization is enabled, false otherwise
   */
  @Override
  public Boolean status(Long apiId) {
    return new BizTemplate<Boolean>() {
      ApisBaseInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Verify API exists and retrieve API base info
        apiInfoDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected Boolean process() {
        return apiInfoDb.getAuth();
      }
    }.execute();
  }

  /**
   * Gets user permissions for a specific API with admin privilege handling.
   *
   * <p>This method retrieves all permissions granted to a user for a specific API,
   * including special handling for admin users and API creators.</p>
   *
   * <p>The method considers admin privileges and creator permissions when
   * determining the final permission set.</p>
   *
   * @param apiId  the API ID to check permissions for
   * @param userId the user ID to get permissions for
   * @param admin  whether to consider admin privileges
   * @return list of permissions granted to the user
   */
  @Override
  public List<ApiPermission> userAuth(Long apiId, Long userId, Boolean admin) {
    return new BizTemplate<List<ApiPermission>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Verify API exists and retrieve API base info
        apisDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected List<ApiPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return ApiPermission.ALL;
        }

        List<ApisAuth> apisAuths = findAuth(userId, apiId);
        if (isCreator(apisAuths)) {
          return ApiPermission.ALL;
        }

        Set<ApiPermission> permissions = new HashSet<>();
        if (!apisDb.isEnabledAuth()) {
          permissions.add(ApiPermission.VIEW);
          permissions.add(ApiPermission.DEBUG);
        }

        Set<ApiPermission> authPermissions = apisAuths.stream().map(ApisAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  /**
   * Gets current user authorization status for a specific API.
   *
   * <p>This method retrieves the current user's authorization status for a specific API,
   * including project authorization, API authorization, and detailed permission set.</p>
   *
   * <p>The method considers admin privileges, creator permissions, and public access
   * when determining the final authorization status.</p>
   *
   * @param apiId the API ID to check current user authorization for
   * @param admin whether to consider admin privileges
   * @return the current user's authorization status with detailed permissions
   */
  @Override
  public ApisAuthCurrent currentUserAuth(Long apiId, Boolean admin) {
    return new BizTemplate<ApisAuthCurrent>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Verify API exists and retrieve API base info
        apisDb = apisQuery.checkAndFindBaseInfo(apiId);
      }

      @Override
      protected ApisAuthCurrent process() {
        // Initialize authorization status with project and API authorization flags
        ApisAuthCurrent authCurrent = new ApisAuthCurrent();
        authCurrent.setProjectAuth(apisDb.getServiceAuth());
        authCurrent.setApisAuth(apisDb.getAuth());

        // Grant all permissions for admin users if admin flag is set
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(ApiPermission.ALL);
          return authCurrent;
        }

        // Grant all permissions for API creators
        List<ApisAuth> apisAuths = findAuth(getUserId(), apiId);
        if (isCreator(apisAuths)) {
          authCurrent.addPermissions(ApiPermission.ALL);
          return authCurrent;
        }

        // Add default permissions for APIs without authorization control
        Set<ApiPermission> permissions = new HashSet<>();
        if (!apisDb.isEnabledAuth()) {
          permissions.add(ApiPermission.VIEW);
          permissions.add(ApiPermission.DEBUG);
        }

        // Aggregate all granted permissions from authorization records
        Set<ApiPermission> authPermissions = apisAuths.stream().map(ApisAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public void check(Long apisId, ApiPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, apisId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<ApisAuth> find(List<String> apiIds, Specification<ApisAuth> spec,
      Pageable pageable) {
    return new BizTemplate<Page<ApisAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(apiIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"apisId"});
        batchCheckPermission(apiIds.stream().map(Long::parseLong)
            .collect(Collectors.toSet()), ApiPermission.VIEW);
      }

      @Override
      protected Page<ApisAuth> process() {
        return apisAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ApisAuth checkAndFind(Long id) {
    return apisAuthRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "ApiAuth"));
  }

  @Override
  public void checkViewAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.DELETE);
  }

  @Override
  public void checkDebugAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.DEBUG);
  }

  @Override
  public void checkTestAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.TEST);
  }

  /**
   * Checks if user has grant authorization for an API.
   *
   * <p>This method validates whether a user has permission to grant access to an API.
   * Public APIs can be modified and authorized by anyone.</p>
   *
   * @param userId the user ID to check grant authorization for
   * @param apisId the API ID to check grant authorization for
   * @throws BizException if user lacks grant authorization
   */
  @Override
  public void checkGrantAuth(Long userId, Long apisId) {
    // Note: Public APIs can be modified and authorized by anyone
    checkAuth(userId, apisId, ApiPermission.GRANT, false, true);
  }

  @Override
  public void checkShareAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.SHARE);
  }

  /**
   * Checks if user has release authorization for an API.
   *
   * <p>This method validates whether a user has permission to release an API.
   * Release permission is also required for modifying the released status of public APIs.</p>
   *
   * @param userId the user ID to check release authorization for
   * @param apisId the API ID to check release authorization for
   * @throws BizException if user lacks release authorization
   */
  @Override
  public void checkReleaseAuth(Long userId, Long apisId) {
    // Note: Release permission is also required for modifying the released status of public APIs
    checkAuth(userId, apisId, ApiPermission.RELEASE, false, true);
  }

  @Override
  public void checkExportAuth(Long userId, Long apisId) {
    checkAuth(userId, apisId, ApiPermission.EXPORT);
  }

  @Override
  public void checkAuth(Long userId, Long apisId, ApiPermission permission) {
    checkAuth(userId, apisId, permission, false,
        permission.isRelease() || permission.isGrant());
  }

  /**
   * Checks user authorization for a specific permission with advanced options.
   *
   * <p>This method provides comprehensive authorization checking with options to ignore
   * admin permissions and public access controls.</p>
   *
   * <p>The method handles special cases for grant and release permissions on public APIs,
   * and validates creator permissions and flat permission sets.</p>
   *
   * @param userId                the user ID to check authorization for
   * @param apisId                the API ID to check authorization for
   * @param permission            the permission to check
   * @param ignoreAdminPermission whether to ignore admin user privileges
   * @param ignorePublicAccess    whether to ignore public access controls
   * @throws BizException if user lacks the required permission
   */
  @Override
  public void checkAuth(Long userId, Long apisId, ApiPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    // Skip validation for admin users or non-user actions
    if (!ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Skip validation for non-grant/non-release permissions on APIs without authorization control
    // Note: This prevents users without authorization permissions from granting access
    if (!ignorePublicAccess && !permission.isGrant() && !permission.isRelease()
        && !apisQuery.isAuthCtrl(apisId)) {
      return;
    }

    // Validate view permissions as base permissions
    List<ApisAuth> auths = findAuth(userId, apisId);
    if (permission.equals(ApiPermission.VIEW)) {
      if (isEmpty(auths)) {
        throw BizException.of(APIS_NO_AUTH_CODE, APIS_NO_AUTH, new Object[]{permission});
      }
    }

    // Grant all permissions to API creators
    if (isCreator(auths)) {
      return;
    }

    // Validate specific permission in flat permission set
    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(APIS_NO_AUTH_CODE, APIS_NO_AUTH, new Object[]{permission});
    }
  }

  /**
   * Verifies operation permissions for multiple APIs in batch.
   *
   * <p>This method performs batch permission validation for multiple APIs,
   * checking if the current user has the specified permission for all APIs.</p>
   *
   * <p>The method handles admin user privileges and filters APIs that require
   * authorization control for efficient validation.</p>
   *
   * @param apiIds     the collection of API IDs to check permissions for
   * @param permission the permission to validate for all APIs
   * @throws BizException if user lacks permission for any API in the batch
   */
  @Override
  public void batchCheckPermission(Collection<Long> apiIds, ApiPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(apiIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? apiIds : apisBaseInfoRepo.findIds0ByIdInAndAuth(apiIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ApisAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      ApisBaseInfo apisBaseInfo = apisBaseInfoRepo.findById(firstId).orElse(null);
      throw BizException.of(APIS_NO_TARGET_AUTH_CODE, APIS_NO_TARGET_AUTH, new Object[]{permission,
          isNull(apisBaseInfo) ? firstId : apisBaseInfo.getName()});
    }

    Map<Long, List<ApisAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getApisId())).collect(Collectors.groupingBy(ApisAuth::getApisId));
    for (Long apiId : authMap.keySet()) {
      List<ApisAuth> values = authMap.get(apiId);
      if (isNotEmpty(values)) {
        List<ApiPermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).toList();
        if (isNotEmpty(permissions) && permissions.contains(permission)) {
          continue;
        }
      }
      ApisBaseInfo apisBaseInfo = apisBaseInfoRepo.findById(apiId).orElse(null);
      throw BizException.of(APIS_NO_TARGET_AUTH_CODE, APIS_NO_TARGET_AUTH, new Object[]{permission,
          isNull(apisBaseInfo) ? apiId : apisBaseInfo.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creator) {
    if (apisAuthRepo.countByApisIdAndAuthObjectIdAndAuthObjectTypeAndCreator(apisId,
        authObjectId, authObjectType, creator) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType) {
    if (apisAuthRepo
        .countByApisIdAndAuthObjectIdAndAuthObjectType(apisId, authObjectId, authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<ApisAuth> findAuth(Long userId, Long apisId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return apisAuthRepo.findAllByApisIdAndAuthObjectIdIn(apisId, orgIds);
  }

  @Override
  public List<ApisAuth> findAuth(Long userId, Collection<Long> apisIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(apisIds) ? apisAuthRepo.findAllByAuthObjectIdIn(orgIds) :
        apisAuthRepo.findAllByApisIdInAndAuthObjectIdIn(apisIds, orgIds);
  }

  @Override
  public List<ApiPermission> getUserAuth(Long apiId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return ApiPermission.ALL;
    }

    List<ApisAuth> apisAuths = findAuth(userId, apiId);
    if (isEmpty(apisAuths)) {
      return null;
    }
    if (isCreator(apisAuths)) {
      return ApiPermission.ALL;
    }
    return apisAuths.stream().map(ApisAuth::getAuths).flatMap(Collection::stream)
        .distinct().toList();
  }

  @Override
  public boolean isCreator(Long userId, Long apisId) {
    List<ApisAuth> apisAuths = findAuth(userId, apisId);
    return isCreator(apisAuths);
  }

  private boolean isCreator(List<ApisAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ApisAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  private Set<ApiPermission> flatPermissions(List<ApisAuth> auths) {
    Set<ApiPermission> actions = new HashSet<>();
    for (ApisAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }
}
