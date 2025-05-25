package cloud.xcan.angus.core.tester.application.cmd.task.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_STATUS_MISMATCH_T;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus.BLOCKED;
import static cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus.COMPLETED;
import static cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus.IN_PROGRESS;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskSprintAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.converter.TaskSprintConverter;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.task.TaskSprintQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class TaskSprintCmdImpl extends CommCmd<TaskSprint, Long> implements TaskSprintCmd {

  @Resource
  private TaskSprintRepo taskSprintRepo;

  @Resource
  private TaskSprintQuery taskSprintQuery;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private TaskSprintAuthCmd taskSprintAuthCmd;

  @Resource
  private TaskSprintAuthQuery taskSprintAuthQuery;

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private TaskCmd taskCmd;

  @Resource
  private TaskTrashRepo trashTaskRepo;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Resource
  private UserManager userManager;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskSprint sprint) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(getUserId(), sprint.getProjectId());

        // Check the project exists
        projectQuery.checkAndFind(sprint.getProjectId());

        // Check the sprint name exists
        taskSprintQuery.checkNameExists(sprint.getProjectId(), sprint.getName());
        // Check the sprint date range
        // NOOP:: taskSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getDeadlineDate());
        // Check the quota limit
        taskSprintQuery.checkQuota();

        // Check the owner exists
        userManager.checkAndFind(sprint.getOwnerId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(sprint);

        // Init sprint auth
        taskSprintAuthCmd.addCreatorAuth(idKey.getId(), Set.of(getUserId()));

        activityCmd.add(toActivity(TASK_SPRINT, sprint, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(TaskSprint sprint) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(sprint.getId());

        // Check the sprint name exists
        if (isNotEmpty(sprint.getName()) && !sprintDb.getName().equals(sprint.getName())) {
          taskSprintQuery.checkNameExists(sprintDb.getProjectId(), sprint.getName());
        }

        // Check the sprint date range
        // Fix:: The time of modification can not be a future value.
        // if (nonNull(sprint.getStartDate()) && nonNull(sprint.getEndDate())) {
        //  funcSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getEndDate());
        //}

        // Check the sprint permission
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), sprintDb.getId());
        // Check the owner exists
        if (nonNull(sprint.getOwnerId())) {
          userManager.checkAndFind(sprint.getOwnerId());
        }
      }

      @Override
      protected Void process() {
        if (nonNull(sprint.getEvalWorkloadMethod())
            && !sprint.getEvalWorkloadMethod().equals(sprintDb.getEvalWorkloadMethod())) {
          taskRepo.updateEvalWorkloadMethodBySprintId(sprint.getId(),
              sprint.getEvalWorkloadMethod());
        }

        updateOrNotFound0(sprint);

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(TaskSprint sprint) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        if (nonNull(sprint.getId())) {
          // Check the sprint exists
          sprintDb = taskSprintQuery.checkAndFind(sprint.getId());

          // Check the sprint name exists
          if (!sprintDb.getName().equals(sprint.getName())) {
            taskSprintQuery.checkNameExists(sprintDb.getProjectId(), sprint.getName());
          }

          // Check the sprint date range
          // Fix:: The time of modification can not be a future value.
          // funcSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getEndDate());

          // Check the sprint permission
          taskSprintAuthQuery.checkModifySprintAuth(getUserId(), sprint.getId());
          // Check the owner exists
          userManager.checkAndFind(sprint.getOwnerId());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(sprint.getId())) {
          return add(sprint);
        }

        if (nonNull(sprint.getEvalWorkloadMethod())
            && !sprint.getEvalWorkloadMethod().equals(sprintDb.getEvalWorkloadMethod())) {
          taskRepo.updateEvalWorkloadMethodBySprintId(sprint.getId(),
              sprint.getEvalWorkloadMethod());
        }

        TaskSprintConverter.setReplaceInfo(sprint, sprintDb);
        taskSprintRepo.save(sprint);

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(sprintDb.getId()).setKey(sprintDb.getName());
      }
    }.execute();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Check the modify permission
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Check the status is allowed
        ProtocolAssert.assertTrue(sprintDb.getStatus().allowStart(), TASK_SPRINT_STATUS_MISMATCH_T,
            new Object[]{sprintDb.getStatus(), IN_PROGRESS});
      }

      @Override
      protected Void process() {
        sprintDb.setStatus(IN_PROGRESS);
        taskSprintRepo.save(sprintDb);

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, STATUS_UPDATE, IN_PROGRESS));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void end(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Check the modify permission
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Check the status is allowed
        ProtocolAssert.assertTrue(sprintDb.getStatus().allowEnd(), TASK_SPRINT_STATUS_MISMATCH_T,
            new Object[]{sprintDb.getStatus(), COMPLETED});

        // Check the tasks is completed
        taskSprintQuery.checkSprintTasksCompleted(id);
      }

      @Override
      protected Void process() {
        sprintDb.setStatus(COMPLETED);
        taskSprintRepo.save(sprintDb);

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, STATUS_UPDATE, COMPLETED));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void block(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Check the modify permission
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Check the status is allowed
        ProtocolAssert.assertTrue(sprintDb.getStatus().allowBlock(), TASK_SPRINT_STATUS_MISMATCH_T,
            new Object[]{sprintDb.getStatus(), BLOCKED});
      }

      @Override
      protected Void process() {
        sprintDb.setStatus(BLOCKED);
        taskSprintRepo.save(sprintDb);

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, STATUS_UPDATE, BLOCKED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        TaskSprint newSprint = TaskSprintConverter.clone(sprintDb);
        taskSprintQuery.setSafeCloneName(newSprint);
        IdKey<Long, Object> idKey = insert(newSprint, "name");

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, ActivityType.CLONE, sprintDb.getName()));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(Long id, Long targetProjectId) {
    new BizTemplate<Void>() {
      Project targetProjectDb;
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Check if the movement position has changed
        ProtocolAssert.assertTrue(!sprintDb.getProjectId().equals(targetProjectId),
            "The moving position has not changed");

        // Check the project exists
        targetProjectDb = projectQuery.checkAndFind(targetProjectId);

        // Check the to have permission to modify the sprint
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Note: After moving to the target projectectory, you have resource permissions under the projectectory.
        // Unlike scenarios and apis, you do not need to authorize the resource's permissions to target project creators

        // Bug:: Grant permission
        // 1. Authorize new creator permissions for moving projectectories
        ////  2. @DoInFuture: Authorize the auth objects of the original projectectory to view the target projectectory
        // List<Project> targetParentProjects = funcProjectQuery.getTargetParentProject(targetProjectId);
        // Set<Long> newCreatorIds = funcProjectQuery.getTargetParentCreatorIds(
        //    List.of(sprintDb.getCreatedBy()), targetParentProjects);
        // funcProjectAuthCmd.moveCreatorAuth(id, newCreatorIds,
        //    targetParentProjects.stream().map(Project::getId).collect(Collectors.toSet()));

        // Modify sprint projectId
        taskSprintRepo.updateProjectById(id, targetProjectId);
        // Modify task projectId
        taskRepo.updateProjectBySprintId(id, targetProjectId);

        // NOOP: Init sprint creator to view parent project permissions by WEB-UI

        // Add move sprint activity
        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, MOVED_TO, targetProjectDb.getName()));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void restart(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<TaskSprint> sprintsDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintsDb = taskSprintQuery.checkAndFind(ids);

        // Check the sprint permission
        taskSprintAuthQuery.batchCheckPermission(ids, TaskSprintPermission.RESTART_TASK);
      }

      @Override
      protected Void process() {
        taskRepo.updateResultToRestartBySprintIds(ids);

        startSprintIfCompleted(sprintsDb);

        activityCmd.addAll(toActivities(TASK_SPRINT, sprintsDb, ActivityType.TASK_RESTART));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reopen(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<TaskSprint> sprintsDb;

      @Override
      protected void checkParams() {
        // Check the sprint exists
        sprintsDb = taskSprintQuery.checkAndFind(ids);

        // Check the sprint permission
        taskSprintAuthQuery.batchCheckPermission(ids, TaskSprintPermission.REOPEN_TASK);
      }

      @Override
      protected Void process() {
        taskRepo.updateResultToReopenBySprintIds(ids);

        startSprintIfCompleted(sprintsDb);

        activityCmd.addAll(toActivities(TASK_SPRINT, sprintsDb, ActivityType.TASK_REOPEN));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<TaskSprint> sprintDb;

      @Override
      protected void checkParams() {
        // Check the sprint existed and authed
        sprintDb = taskSprintRepo.findById(id);
        if (sprintDb.isEmpty()) {
          return;
        }
        taskSprintAuthQuery.checkDeleteSprintAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        if (sprintDb.isEmpty()) {
          return null;
        }

        TaskSprint sprint0 = sprintDb.get();

        // Logic delete
        sprint0.setDeleted(true).setDeletedBy(getUserId()).setDeletedDate(LocalDateTime.now());
        taskSprintRepo.save(sprint0);

        // Fix:: Do not delete the tasks after deleting the sprint.
        taskRepo.updateSprintDeleteStatusBySprint(Collections.singleton(id), true);

        // Add sprint to Trash
        trashTaskRepo.save(TaskSprintConverter.toTrashTask(sprint0));

        // Add delete sprint activity
        activityCmd.add(toActivity(TASK_SPRINT, sprint0, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void delete0(List<Long> ids) {
    // Delete sprint
    taskSprintRepo.deleteAllByIdIn(ids);

    List<Long> taskIds = taskRepo.findAll0IdBySprintIdIn(ids);
    if (isNotEmpty(taskIds)) {
      taskCmd.delete0(taskIds);
    }
  }

  private void startSprintIfCompleted(List<TaskSprint> sprintsDb) {
    List<TaskSprint> completedSprintDb = sprintsDb.stream()
        .filter(x -> x.getStatus().isCompleted()).collect(Collectors.toList());
    if (isNotEmpty(completedSprintDb)) {
      for (TaskSprint sprint : completedSprintDb) {
        sprint.setStatus(TaskSprintStatus.PENDING);
      }
      taskSprintRepo.saveAll(completedSprintDb);
    }
  }

  @Override
  protected BaseRepository<TaskSprint, Long> getRepository() {
    return this.taskSprintRepo;
  }
}
