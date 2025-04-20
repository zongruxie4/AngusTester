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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskSprintAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the project exists
        sprintDb = taskSprintQuery.checkAndFind(auth.getSprintId());
        // Check the add creator permissions
        BizAssert.assertTrue(!sprintDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the user have project authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), auth.getSprintId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        // Check the for duplicate authorizations
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(TaskSprintAuth auth) {
    new BizTemplate<Void>() {
      TaskSprintAuth authDb;
      TaskSprint sprintDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the sprint authorization existed
        authDb = taskSprintAuthQuery.checkAndFind(auth.getId());
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(authDb.getSprintId());
        // Check the current user have sprint authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), authDb.getSprintId());
        // Check the authorization object exists
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long sprintId, Boolean enabled) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint existed and authed
        sprintDb = taskSprintQuery.checkAndFind(sprintId);
        // Check the user have sprint authorization permissions
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      TaskSprintAuth authDb;
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint auth exists
        authDb = taskSprintAuthQuery.checkAndFind(id);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(authDb.getSprintId());
        // Check the user have sprint authorization permissions
        taskSprintAuthQuery.checkGrantAuth(getUserId(), authDb.getSprintId());
      }

      @Override
      protected Void process() {
        // Get if authorization object name
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // NOOP: Authorization can also be cancelled after the authorization object is deleted
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, AUTH_CANCEL, authObjectName));

        // Delete sprint permission
        taskSprintAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Override
  public void addCreatorAuth(Long sprintId, Set<Long> creatorIds) {
    batchInsert(toTaskSprintAuths(creatorIds, sprintId, uidGenerator), "authObjectId");
  }

  @Override
  protected BaseRepository<TaskSprintAuth, Long> getRepository() {
    return this.taskSprintAuthRepo;
  }
}




