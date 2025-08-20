package cloud.xcan.angus.core.tester.application.query.task.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NO_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_NO_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthCurrent;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * <p>
 * Implementation of TaskSprintAuthQuery for task sprint authorization management and validation.
 * </p>
 * <p>
 * Provides comprehensive authorization services for task sprints including permission checks,
 * user authentication, admin validation, and batch permission verification. Supports both
 * individual and batch authorization operations with proper error handling and resource validation.
 * </p>
 */
@Biz
public class TaskSprintAuthQueryImpl implements TaskSprintAuthQuery {

  @Resource
  private TaskSprintAuthRepo taskSprintAuthRepo;
  @Resource
  private TaskSprintRepo taskSprintRepo;
  @Resource
  private TaskSprintQuery taskSprintQuery;
  @Resource
  private UserRepo userRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * <p>
   * Get the authorization status of a task sprint.
   * </p>
   * <p>
   * Retrieves whether authorization control is enabled for the specified sprint.
   * Validates that the sprint exists before returning its authorization status.
   * </p>
   * @param sprintId Sprint ID
   * @return Boolean indicating if authorization is enabled for the sprint
   */
  @Override
  public Boolean status(Long sprintId) {
    return new BizTemplate<Boolean>() {
      TaskSprint taskSprintDb;

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        taskSprintDb = taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected Boolean process() {
        return taskSprintDb.getAuth();
      }
    }.execute();
  }

  /**
   * <p>
   * Get user authorization permissions for a task sprint.
   * </p>
   * <p>
   * Retrieves all permissions granted to a specific user for the sprint. Includes admin bypass
   * logic and creator privilege checks. Returns all permissions for admin users or creators.
   * </p>
   * @param sprintId Sprint ID
   * @param userId User ID to check permissions for
   * @param admin Whether to check admin privileges
   * @return List of permissions granted to the user
   */
  @Override
  public List<TaskSprintPermission> userAuth(Long sprintId, Long userId, Boolean admin) {
    return new BizTemplate<List<TaskSprintPermission>>() {

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected List<TaskSprintPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return TaskSprintPermission.ALL;
        }

        List<TaskSprintAuth> sprintAuths = findAuth(userId, sprintId);
        if (isCreator(sprintAuths)) {
          return TaskSprintPermission.ALL;
        }

        return sprintAuths.stream().map(TaskSprintAuth::getAuths)
            .flatMap(Collection::stream).distinct().toList();
      }
    }.execute();
  }

  /**
   * <p>
   * Get current user's authorization status and permissions for a task sprint.
   * </p>
   * <p>
   * Retrieves the current user's authorization information including sprint auth status
   * and granted permissions. Handles admin privileges and creator status automatically.
   * </p>
   * @param sprintId Sprint ID
   * @param admin Whether to check admin privileges
   * @return Current user's authorization information
   */
  @Override
  public TaskSprintAuthCurrent currentUserAuth(Long sprintId, Boolean admin) {
    return new BizTemplate<TaskSprintAuthCurrent>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the task sprint exists
        sprintDb = taskSprintQuery.checkAndFind(sprintId);
      }

      @Override
      protected TaskSprintAuthCurrent process() {
        TaskSprintAuthCurrent authCurrent = new TaskSprintAuthCurrent();
        authCurrent.setTaskSprintAuth(sprintDb.getAuth());

        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(TaskSprintPermission.ALL);
          return authCurrent;
        }

        List<TaskSprintAuth> sprintAuths = findAuth(getUserId(), sprintId);
        if (isCreator(sprintAuths)) {
          authCurrent.addPermissions(TaskSprintPermission.ALL);
          return authCurrent;
        }

        authCurrent.addPermissions(sprintAuths.stream()
            .map(TaskSprintAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet()));
        return authCurrent;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if a user has a specific permission for a sprint.
   * </p>
   * <p>
   * Core authorization validation method that checks user permissions for a sprint.
   * Handles admin bypass, public access, and creator privileges automatically.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   * @param permission Required permission to check
   */
  @Override
  public void checkAuth(Long userId, Long sprintId, TaskSprintPermission permission) {
    checkAuth(userId, sprintId, permission, false, permission.isGrant());
  }

  /**
   * <p>
   * Check if a user has a specific permission for a sprint with advanced options.
   * </p>
   * <p>
   * Extended authorization validation method with options to ignore admin permissions
   * and public access controls. Used for special authorization scenarios.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID (nullable for backlog scenarios)
   * @param permission Required permission to check
   * @param ignoreAdminPermission Whether to ignore admin user privileges
   * @param ignorePublicAccess Whether to ignore public access controls
   */
  @Override
  public void checkAuth(Long userId, @Nullable Long sprintId, TaskSprintPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (isNull(sprintId) /* Fix: Backlog or general project management sprint is null */
        || !ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.notIgnorePublicAccess()
        && !taskSprintQuery.isAuthCtrl(sprintId)) {
      return;
    }

    List<TaskSprintAuth> projectAuths = findAuth(userId, sprintId);
    if (isCreator(projectAuths)) {
      return;
    }
    BizAssert.assertTrue(flatPermissions(projectAuths).contains(permission),
        TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH, new Object[]{permission});
  }

  /**
   * <p>
   * Check if a user has a specific permission for a task sprint.
   * </p>
   * <p>
   * Validates that the specified user has the required permission for the sprint.
   * Throws authorization exception if the user lacks the required permission.
   * </p>
   * @param sprintId Sprint ID
   * @param permission Required permission to check
   * @param userId User ID to check permission for
   */
  @Override
  public void check(Long sprintId, TaskSprintPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, sprintId, permission);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Find task sprint authorizations with pagination support.
   * </p>
   * <p>
   * Searches for sprint authorizations based on specification criteria and sprint IDs.
   * Supports pagination and filtering with proper parameter validation.
   * </p>
   * @param spec Generic specification for filtering
   * @param sprintIds List of sprint IDs to search within
   * @param pageable Pagination parameters
   * @return Paginated results of sprint authorizations
   */
  @Override
  public Page<TaskSprintAuth> find(GenericSpecification<TaskSprintAuth> spec, List<String> sprintIds,
      Pageable pageable) {
    return new BizTemplate<Page<TaskSprintAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(sprintIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"sprintId"});
      }

      @Override
      protected Page<TaskSprintAuth> process() {
        return taskSprintAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find a task sprint authorization by ID.
   * </p>
   * <p>
   * Retrieves a sprint authorization entity by its ID and throws ResourceNotFound if not found.
   * Used for validation before operations that require an existing authorization record.
   * </p>
   * @param id Sprint authorization ID
   * @return TaskSprintAuth entity if found
   * @throws ResourceNotFound if the authorization is not found
   */
  @Override
  public TaskSprintAuth checkAndFind(Long id) {
    return taskSprintAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "TaskSprintAuth"));
  }

  /**
   * <p>
   * Check if a user has permission to modify a task sprint.
   * </p>
   * <p>
   * Validates that the specified user has MODIFY_SPRINT permission for the sprint.
   * Throws authorization exception if the user lacks the required permission.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   */
  @Override
  public void checkModifySprintAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.MODIFY_SPRINT);
  }

  /**
   * <p>
   * Check if a user has permission to delete a task sprint.
   * </p>
   * <p>
   * Validates that the specified user has DELETE_SPRINT permission for the sprint.
   * Throws authorization exception if the user lacks the required permission.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   */
  @Override
  public void checkDeleteSprintAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.DELETE_SPRINT);
  }

  /**
   * <p>
   * Check if a user has permission to add tasks to a sprint.
   * </p>
   * <p>
   * Validates that the specified user has ADD_TASK permission for the sprint.
   * Throws authorization exception if the user lacks the required permission.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   */
  @Override
  public void checkAddTaskAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.ADD_TASK);
  }

  /**
   * <p>
   * Check if a user has permission to modify tasks in a sprint.
   * </p>
   * <p>
   * Validates that the specified user has MODIFY_TASK permission for the sprint.
   * Throws authorization exception if the user lacks the required permission.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   */
  @Override
  public void checkModifyTaskAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.MODIFY_TASK);
  }

  /**
   * <p>
   * Check if a user has permission to grant authorization for a sprint.
   * </p>
   * <p>
   * Validates that the specified user has GRANT permission for the sprint.
   * This permission allows users to grant authorization to other users.
   * </p>
   * @param userId User ID to check permission for
   * @param sprintId Sprint ID
   */
  @Override
  public void checkGrantAuth(Long userId, Long sprintId) {
    checkAuth(userId, sprintId, TaskSprintPermission.GRANT, false, true);
  }

  /**
   * <p>
   * Batch check permissions for multiple sprints.
   * </p>
   * <p>
   * Efficiently validates permissions across multiple sprints for the current user.
   * Optimizes database queries by filtering sprints that require authorization control.
   * </p>
   * @param sprintIds Collection of sprint IDs to check
   * @param permission Required permission to check
   */
  @Override
  public void batchCheckPermission(Collection<Long> sprintIds, TaskSprintPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(sprintIds) || isNull(permission) || !isUserAction()) {
      return;
    }

    // Filter sprints that require authorization control for better performance
    Collection<Long> authIds = permission.isGrant()
        ? sprintIds : taskSprintRepo.findIds0ByIdInAndAuth(sprintIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    // Batch retrieve all authorizations for the current user
    List<TaskSprintAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      TaskSprint sprint = taskSprintRepo.findById(firstId).orElse(null);
      BizAssert.assertTrue(false, TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH,
          new Object[]{permission, Objects.isNull(sprint) ? firstId : sprint.getName()});
    }

    // Group authorizations by sprint ID for efficient lookup
    Map<Long, List<TaskSprintAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getSprintId()))
        .collect(Collectors.groupingBy(TaskSprintAuth::getSprintId));

    // Check permissions for each sprint
    for (Long sprintId : authMap.keySet()) {
      List<TaskSprintAuth> values = authMap.get(sprintId);
      if (isNotEmpty(values)) {
        // Extract all permissions from the authorization records
        Set<TaskSprintPermission> sprintPermissions = values.stream()
            .flatMap(o -> o.getAuths().stream())
            .collect(Collectors.toSet());

        // Check if the required permission is present
        if (sprintPermissions.contains(permission)) {
          continue;
        }
      }

      // Permission not found, throw exception with sprint details
      TaskSprint sprint = taskSprintRepo.findById(sprintId).orElse(null);
      BizAssert.assertTrue(false, TASK_SPRINT_NO_AUTH_CODE, TASK_SPRINT_NO_AUTH,
          new Object[]{permission, Objects.isNull(sprint) ? sprintId : sprint.getName()});
    }
  }

  /**
   * <p>
   * Check for duplicate authorization entries.
   * </p>
   * <p>
   * Validates that no duplicate authorization exists for the same auth object
   * in the specified sprint. Prevents duplicate authorization assignments.
   * </p>
   * @param sprintId Sprint ID
   * @param authObjectId Auth object ID
   * @param authObjectType Auth object type
   * @throws ResourceExisted if duplicate authorization exists
   */
  @Override
  public void checkRepeatAuth(Long sprintId, Long authObjectId, AuthObjectType authObjectType) {
    if (taskSprintAuthRepo.countBySprintIdAndAuthObjectIdAndAuthObjectType(sprintId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  /**
   * <p>
   * Find sprint IDs where a user has a specific permission.
   * </p>
   * <p>
   * Retrieves all sprint IDs where the specified user (including their organizations)
   * has the given permission. Includes both direct user permissions and organization-based permissions.
   * </p>
   * @param userId User ID
   * @param permission Required permission
   * @return List of sprint IDs where the user has the permission
   */
  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId,
      TaskSprintPermission permission) {
    // Get user's organization IDs and add user ID for comprehensive permission check
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);

    return taskSprintAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(auth -> auth.getAuths().contains(permission))
        .map(TaskSprintAuth::getSprintId)
        .distinct()
        .toList();
  }

  /**
   * <p>
   * Find authorization records for a user in a specific sprint.
   * </p>
   * <p>
   * Retrieves all authorization records for the user (including their organizations)
   * in the specified sprint. Includes both direct user permissions and organization-based permissions.
   * </p>
   * @param userId User ID
   * @param sprintId Sprint ID
   * @return List of authorization records
   */
  @Override
  public List<TaskSprintAuth> findAuth(Long userId, Long sprintId) {
    // Get user's organization IDs and add user ID for comprehensive permission check
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return taskSprintAuthRepo.findAllBySprintIdAndAuthObjectIdIn(sprintId, orgIds);
  }

  /**
   * <p>
   * Find authorization records for a user across multiple sprints.
   * </p>
   * <p>
   * Retrieves all authorization records for the user (including their organizations)
   * across the specified sprints. If no sprint IDs provided, returns all user authorizations.
   * </p>
   * @param userId User ID
   * @param projectIds Collection of sprint IDs (optional)
   * @return List of authorization records
   */
  @Override
  public List<TaskSprintAuth> findAuth(Long userId, Collection<Long> projectIds) {
    // Get user's organization IDs and add user ID for comprehensive permission check
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);

    // Return all user authorizations if no specific sprints provided
    return isEmpty(projectIds)
        ? taskSprintAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : taskSprintAuthRepo.findAllBySprintIdInAndAuthObjectIdIn(projectIds, orgIds);
  }

  /**
   * <p>
   * Get all permissions for a user in a specific sprint.
   * </p>
   * <p>
   * Retrieves all permissions granted to the user for the sprint, including admin privileges
   * and creator status. Returns null if no permissions are found.
   * </p>
   * @param sprintId Sprint ID
   * @param userId User ID
   * @return List of permissions, or null if none found
   */
  @Override
  public List<TaskSprintPermission> getUserAuth(Long sprintId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return TaskSprintPermission.ALL;
    }

    List<TaskSprintAuth> auths = findAuth(userId, sprintId);
    if (isEmpty(auths)) {
      return null;
    }
    if (isCreator(auths)) {
      return TaskSprintPermission.ALL;
    }
    return auths.stream().map(TaskSprintAuth::getAuths)
        .flatMap(Collection::stream).distinct().toList();
  }

  /**
   * <p>
   * Check if a user is the creator of a sprint.
   * </p>
   * <p>
   * Determines whether the specified user is the creator of the sprint
   * by checking their authorization records.
   * </p>
   * @param userId User ID
   * @param sprintId Sprint ID
   * @return true if the user is the creator, false otherwise
   */
  @Override
  public boolean isCreator(Long userId, Long sprintId) {
    return isCreator(findAuth(userId, sprintId));
  }

  /**
   * <p>
   * Check if the current user is an admin user.
   * </p>
   * <p>
   * Delegates to CommonQuery to determine if the current user has admin privileges.
   * </p>
   * @return true if the current user is an admin, false otherwise
   */
  @Override
  public boolean isAdminUser() {
    return commonQuery.isAdminUser();
  }

  /**
   * <p>
   * Check if any of the authorization records indicates creator status.
   * </p>
   * <p>
   * Helper method to determine if the user is a creator based on their authorization records.
   * </p>
   * @param auths List of authorization records
   * @return true if any record indicates creator status, false otherwise
   */
  private boolean isCreator(List<TaskSprintAuth> auths) {
    return auths.stream().anyMatch(TaskSprintAuth::getCreator);
  }

  /**
   * <p>
   * Flatten all permissions from authorization records into a single set.
   * </p>
   * <p>
   * Helper method to extract and combine all permissions from multiple authorization records,
   * removing duplicates.
   * </p>
   * @param auths List of authorization records
   * @return Set of all unique permissions
   */
  private Set<TaskSprintPermission> flatPermissions(List<TaskSprintAuth> auths) {
    return auths.stream()
        .flatMap(auth -> auth.getAuths().stream())
        .collect(Collectors.toSet());
  }
}
