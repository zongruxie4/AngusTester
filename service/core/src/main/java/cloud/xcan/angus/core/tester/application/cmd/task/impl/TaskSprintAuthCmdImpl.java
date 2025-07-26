package cloud.xcan.angus.core.tester.application.cmd.task.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.TaskSprintAuthConverter.toTaskSprintAuths;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_UPDATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskSprintAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task sprint authorization command operations.
 * 
 * <p>This class provides functionality for managing sprint-level permissions,
 * including authorization grants, updates, and permission control.</p>
 * 
 * <p>It handles the complete lifecycle of sprint authorization from creation
 * to deletion, including creator permission management and activity logging.</p>
 * 
 * <p>Key features include:
 * <ul>
 *   <li>Sprint authorization management</li>
 *   <li>Creator permission handling</li>
 *   <li>Authorization enable/disable control</li>
 *   <li>Activity logging for audit trails</li>
 *   <li>Permission validation and security</li>
 * </ul></p>
 */
@Biz
public class TaskSprintAuthCmdImpl extends CommCmd<TaskSprintAuth, Long> implements TaskSprintAuthCmd {

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private TaskSprintRepo taskSprintRepo;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private TaskSprintAuthQuery taskSprintAuthQuery;

  @Resource
  private TaskSprintAuthRepo taskSprintAuthRepo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds sprint authorization with comprehensive validation.
   * 
   * <p>This method creates a new sprint authorization after verifying
   * sprint existence, permission grants, and preventing creator authorization.</p>
   * 
   * <p>The method logs authorization grant activities for audit purposes.</p>
   * 
   * @param auth the authorization to add
   * @return the ID key of the created authorization
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskSprintAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(auth.getSprintId());
        // Verify creator cannot be granted additional permissions
        BizAssert.assertTrue(!sprintDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Verify user has sprint authorization grant permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), auth.getSprintId());
        // Verify authorization object exists and get its name
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        // Verify no duplicate authorization exists
        taskSprintAuthQuery.checkRepeatAuth(auth.getSprintId(), auth.getAuthObjectId(),
            auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add grant permission activity
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(TASK_SPRINT, sprintDb, ActivityType.AUTH, authObjectName));
        }
        return insert(auth);
      }
    }.execute();
  }

  /**
   * Replaces sprint authorization with comprehensive validation.
   * 
   * <p>This method updates an existing sprint authorization after verifying
   * authorization existence, permission modifications, and creator protection.</p>
   * 
   * <p>The method logs authorization update activities for audit purposes.</p>
   * 
   * @param auth the authorization to replace
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(TaskSprintAuth auth) {
    new BizTemplate<Void>() {
      TaskSprintAuth authDb;
      TaskSprint sprintDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify sprint authorization exists and retrieve it
        authDb = taskSprintAuthQuery.checkAndFind(auth.getId());
        // Verify creator permissions cannot be modified
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(authDb.getSprintId());
        // Verify current user has sprint authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), authDb.getSprintId());
        // Verify authorization object exists and get its name
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Replace authorization
        authDb.setAuths(auth.getAuths());
        taskSprintAuthRepo.save(authDb);

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(TASK_SPRINT, sprintDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Enables or disables sprint authorization control.
   * 
   * <p>This method controls the authorization system for a sprint,
   * enabling or disabling permission checks for all tasks in the sprint.</p>
   * 
   * <p>The method logs authorization enable/disable activities for audit purposes.</p>
   * 
   * @param sprintId the sprint ID to control authorization for
   * @param enabled whether to enable authorization control
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long sprintId, Boolean enabled) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and user has access
        sprintDb = taskSprintQuery.checkAndFind(sprintId);
        // Verify user has sprint authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), sprintId);
      }

      @Override
      protected Void process() {
        taskSprintRepo.updateAuthById(sprintId, enabled);
        taskRepo.updateSprintAuthBySprintId(sprintId, enabled);

        // Enable permission control activity
        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, enabled
            ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Deletes sprint authorization with comprehensive cleanup.
   * 
   * <p>This method removes a sprint authorization after verifying
   * authorization existence, permission modifications, and creator protection.</p>
   * 
   * <p>The method logs authorization cancellation activities for audit purposes.</p>
   * 
   * @param id the authorization ID to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      TaskSprintAuth authDb;
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint authorization exists and retrieve it
        authDb = taskSprintAuthQuery.checkAndFind(id);
        // Verify creator permissions cannot be modified
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(authDb.getSprintId());
        // Verify user has sprint authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), authDb.getSprintId());
      }

      @Override
      protected Void process() {
        // Retrieve authorization object name for activity logging
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // Authorization can be cancelled even if the authorization object is deleted
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, AUTH_CANCEL, authObjectName));

        // Delete sprint permission
        taskSprintAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  /**
   * Adds creator authorization for a sprint.
   * 
   * <p>This method creates creator-level permissions for specified users
   * on a sprint, enabling them to manage the sprint and its tasks.</p>
   * 
   * @param sprintId the sprint ID to add creator authorization for
   * @param creatorIds the set of user IDs to grant creator permissions to
   */
  @Override
  public void addCreatorAuth(Long sprintId, Set<Long> creatorIds) {
    batchInsert(toTaskSprintAuths(creatorIds, sprintId, uidGenerator), "authObjectId");
  }

  /**
   * Returns the repository instance for this command.
   * 
   * @return the task sprint authorization repository
   */
  @Override
  protected BaseRepository<TaskSprintAuth, Long> getRepository() {
    return this.taskSprintAuthRepo;
  }
}




