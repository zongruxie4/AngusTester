package cloud.xcan.angus.core.tester.application.cmd.issue.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_SPRINT_STATUS_MISMATCH_T;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.MOVED_TO;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.STATUS_UPDATE;
import static cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus.BLOCKED;
import static cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus.COMPLETED;
import static cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus.IN_PROGRESS;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskSprintAuthCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.converter.TaskSprintConverter;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintAuthQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskSprintQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprint;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrashRepo;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task sprint command operations for sprint management.
 *
 * <p>This class provides comprehensive functionality for managing task sprints,
 * including creation, status management, task operations, and lifecycle control.</p>
 *
 * <p>It handles the complete sprint lifecycle from creation to completion,
 * including task management, authorization, and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Sprint CRUD operations with comprehensive validation</li>
 *   <li>Sprint status management (start, end, block)</li>
 *   <li>Task operations within sprints (restart, reopen)</li>
 *   <li>Sprint cloning and movement between projects</li>
 *   <li>Authorization and permission management</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
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

  /**
   * Adds a new sprint with comprehensive validation and setup.
   *
   * <p>This method creates a new sprint with extensive validation including
   * project membership, name uniqueness, quota limits, and owner verification.</p>
   *
   * <p>The method automatically initializes creator authorization and logs
   * sprint creation activity.</p>
   *
   * @param sprint the sprint to add
   * @return the ID key of the created sprint
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(TaskSprint sprint) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected void checkParams() {
        // Verify user has project member permissions
        projectMemberQuery.checkMember(getUserId(), sprint.getProjectId());

        // Verify project exists
        projectQuery.checkAndFind(sprint.getProjectId());

        // Verify sprint name is unique within project
        taskSprintQuery.checkNameExists(sprint.getProjectId(), sprint.getName());
        // TODO: Add sprint date range validation if needed
        // taskSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getDeadlineDate());
        // Verify quota limits are not exceeded
        taskSprintQuery.checkQuota();

        // Verify sprint owner exists
        userManager.checkAndFind(sprint.getOwnerId());
      }

      @Override
      protected IdKey<Long, Object> process() {
        sprint.setId(BIDUtils.getId(BIDKey.sprintId));
        IdKey<Long, Object> idKey = insert(sprint);

        // Init sprint auth
        taskSprintAuthCmd.addCreatorAuth(idKey.getId(), Set.of(getUserId()));

        activityCmd.add(toActivity(TASK_SPRINT, sprint, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Updates a sprint with comprehensive validation.
   *
   * <p>This method updates a sprint with extensive validation including
   * name uniqueness, permission checks, and owner verification.</p>
   *
   * <p>The method handles workload method updates and logs sprint
   * update activities.</p>
   *
   * @param sprint the sprint to update
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(TaskSprint sprint) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(sprint.getId());

        // Verify sprint name is unique within project if changed
        if (isNotEmpty(sprint.getName()) && !sprintDb.getName().equals(sprint.getName())) {
          taskSprintQuery.checkNameExists(sprintDb.getProjectId(), sprint.getName());
        }

        // TODO: Add sprint date range validation if needed
        // Note: The time of modification cannot be a future value
        // if (nonNull(sprint.getStartDate()) && nonNull(sprint.getEndDate())) {
        //  funcSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getEndDate());
        //}

        // Verify user has sprint modification permissions
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), sprintDb.getId());
        // Verify sprint owner exists if specified
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

  /**
   * Replaces a sprint with comprehensive validation and activity logging.
   *
   * <p>This method performs a complete replacement of a sprint with extensive
   * validation including name uniqueness, permission checks, and owner verification.</p>
   *
   * <p>The method handles workload method updates and logs sprint
   * replacement activities for audit purposes.</p>
   *
   * @param sprint the sprint to replace
   * @return the ID key of the replaced sprint
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(TaskSprint sprint) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        if (nonNull(sprint.getId())) {
          // Verify sprint exists and retrieve sprint info
          sprintDb = taskSprintQuery.checkAndFind(sprint.getId());

          // Verify sprint name is unique within project if changed
          if (!sprintDb.getName().equals(sprint.getName())) {
            taskSprintQuery.checkNameExists(sprintDb.getProjectId(), sprint.getName());
          }

          // TODO: Add sprint date range validation if needed
          // Note: The time of modification cannot be a future value
          // funcSprintQuery.checkSprintDateRange(sprint.getStartDate(), sprint.getEndDate());

          // Verify user has sprint modification permissions
          taskSprintAuthQuery.checkModifySprintAuth(getUserId(), sprint.getId());
          // Verify sprint owner exists
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

  /**
   * Starts a sprint with status validation.
   *
   * <p>This method changes a sprint status to IN_PROGRESS after verifying
   * the current status allows starting and user has modification permissions.</p>
   *
   * <p>The method logs sprint status update activities for audit purposes.</p>
   *
   * @param id the sprint ID to start
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void start(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Verify user has sprint modification permissions
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Verify current status allows starting
        assertTrue(sprintDb.getStatus().allowStart(), TASK_SPRINT_STATUS_MISMATCH_T,
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

  /**
   * Ends a sprint with comprehensive validation.
   *
   * <p>This method changes a sprint status to COMPLETED after verifying
   * the current status allows ending, user has modification permissions,
   * and all tasks are completed.</p>
   *
   * <p>The method logs sprint status update activities for audit purposes.</p>
   *
   * @param id the sprint ID to end
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void end(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Verify user has sprint modification permissions
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Verify current status allows ending
        assertTrue(sprintDb.getStatus().allowEnd(), TASK_SPRINT_STATUS_MISMATCH_T,
            new Object[]{sprintDb.getStatus(), COMPLETED});

        // Verify all tasks in sprint are completed
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

  /**
   * Blocks a sprint with status validation and activity logging.
   *
   * <p>This method changes a sprint status to BLOCKED after verifying
   * the current status allows blocking and user has modification permissions.</p>
   *
   * <p>The method logs sprint status update activities for audit purposes.</p>
   *
   * @param id the sprint ID to block
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void block(Long id) {
    new BizTemplate<Void>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Verify user has sprint modification permissions
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);

        // Verify current status allows blocking
        assertTrue(sprintDb.getStatus().allowBlock(), TASK_SPRINT_STATUS_MISMATCH_T,
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

  /**
   * Clones a sprint with comprehensive setup and activity logging.
   *
   * <p>This method creates a copy of an existing sprint with all its
   * properties and automatically generates a unique name for the clone.</p>
   *
   * <p>The method logs sprint cloning activities for audit purposes.</p>
   *
   * @param id the sprint ID to clone
   * @return the ID key of the cloned sprint
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> clone(Long id) {
    return new BizTemplate<IdKey<Long, Object>>() {
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(id);
      }

      @Override
      protected IdKey<Long, Object> process() {
        TaskSprint newSprint = TaskSprintConverter.clone(sprintDb);
        newSprint.setId(BIDUtils.getId(BIDKey.sprintId));
        taskSprintQuery.setSafeCloneName(newSprint);
        IdKey<Long, Object> idKey = insert(newSprint, "name");

        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, ActivityType.CLONE, sprintDb.getName()));
        return idKey;
      }
    }.execute();
  }

  /**
   * Moves a sprint to a different project with comprehensive validation.
   *
   * <p>This method moves a sprint and all its tasks to a target project
   * after verifying project existence and user permissions.</p>
   *
   * <p>The method updates project associations and logs sprint movement
   * activities for audit purposes.</p>
   *
   * @param id the sprint ID to move
   * @param targetProjectId the target project ID
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void move(Long id, Long targetProjectId) {
    new BizTemplate<Void>() {
      Project targetProjectDb;
      TaskSprint sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and retrieve sprint info
        sprintDb = taskSprintQuery.checkAndFind(id);

        // Verify movement position has actually changed
        assertTrue(!sprintDb.getProjectId().equals(targetProjectId),
            "The moving position has not changed");

        // Verify target project exists and retrieve project info
        targetProjectDb = projectQuery.checkAndFind(targetProjectId);

        // Verify user has permission to modify the sprint
        taskSprintAuthQuery.checkModifySprintAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Note: After moving to the target project, you have resource permissions under the project.
        // Unlike scenarios and APIs, you do not need to authorize the resource's permissions to target project creators

        // TODO: Grant permission
        // 1. Authorize new creator permissions for moving projects
        // 2. @DoInFuture: Authorize the auth objects of the original project to view the target project
        // List<Project> targetParentProjects = funcProjectQuery.getTargetParentProject(targetProjectId);
        // Set<Long> newCreatorIds = funcProjectQuery.getTargetParentCreatorIds(
        //    List.of(sprintDb.getCreatedBy()), targetParentProjects);
        // funcProjectAuthCmd.moveCreatorAuth(id, newCreatorIds,
        //    targetParentProjects.stream().map(Project::getId).collect(Collectors.toSet()));

        // Update sprint project ID
        taskSprintRepo.updateProjectById(id, targetProjectId);
        // Update task project IDs
        taskRepo.updateProjectBySprintId(id, targetProjectId);

        // TODO: Initialize sprint creator to view parent project permissions via WEB-UI

        // Add move sprint activity
        activityCmd.add(toActivity(TASK_SPRINT, sprintDb, MOVED_TO, targetProjectDb.getName()));
        return null;
      }
    }.execute();
  }

  /**
   * Restarts tasks within sprints with comprehensive cleanup and status reset.
   *
   * <p>This method restarts all tasks within the specified sprints, clearing
   * historical test statistics and status. It also restarts completed sprints
   * to pending status.</p>
   *
   * <p>The method logs task restart activities for audit purposes.</p>
   *
   * @param ids the set of sprint IDs to restart tasks for
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void restart(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<TaskSprint> sprintsDb;

      @Override
      protected void checkParams() {
        // Verify sprints exist and retrieve sprint info
        sprintsDb = taskSprintQuery.checkAndFind(ids);

        // Verify user has restart task permissions for all sprints
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

  /**
   * Reopens tasks within sprints without clearing historical data.
   *
   * <p>This method reopens all tasks within the specified sprints without
   * clearing historical test statistics and status. It also reopens completed
   * sprints to pending status.</p>
   *
   * <p>The method logs task reopen activities for audit purposes.</p>
   *
   * @param ids the set of sprint IDs to reopen tasks for
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void reopen(HashSet<Long> ids) {
    new BizTemplate<Void>() {
      List<TaskSprint> sprintsDb;

      @Override
      protected void checkParams() {
        // Verify sprints exist and retrieve sprint info
        sprintsDb = taskSprintQuery.checkAndFind(ids);

        // Verify user has reopen task permissions for all sprints
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

  /**
   * Deletes a sprint with logical deletion and cleanup.
   *
   * <p>This method performs logical deletion of a sprint after verifying
   * user has deletion permissions. It moves the sprint to trash and
   * updates associated task deletion status.</p>
   *
   * <p>The method logs sprint deletion activities for audit purposes.</p>
   *
   * @param id the sprint ID to delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Optional<TaskSprint> sprintDb;

      @Override
      protected void checkParams() {
        // Verify sprint exists and user has deletion permissions
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

        // Note: Do not delete the tasks after deleting the sprint.
        taskRepo.updateSprintDeleteStatusBySprint(Collections.singleton(id), true);

        // Add sprint to Trash
        trashTaskRepo.save(TaskSprintConverter.toTrashTask(sprint0));

        // Add delete sprint activity
        activityCmd.add(toActivity(TASK_SPRINT, sprint0, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Permanently deletes sprints with cascade cleanup (internal use).
   *
   * <p>This method performs permanent deletion of sprints and all associated
   * tasks without validation or permission checks.</p>
   *
   * <p>Note: This method is intended for internal use and should be called
   * after proper validation and permission checks.</p>
   *
   * @param ids the list of sprint IDs to permanently delete
   */
  @Override
  public void delete0(List<Long> ids) {
    // Permanently delete sprints
    taskSprintRepo.deleteAllByIdIn(ids);

    // Permanently delete associated tasks
    List<Long> taskIds = taskRepo.findAll0IdBySprintIdIn(ids);
    if (isNotEmpty(taskIds)) {
      taskCmd.delete0(taskIds);
    }
  }

  /**
   * Restarts completed sprints to pending status.
   *
   * <p>This method identifies completed sprints and changes their status
   * to pending, allowing them to be restarted.</p>
   *
   * @param sprintsDb the list of sprints to check and restart
   */
  private void startSprintIfCompleted(List<TaskSprint> sprintsDb) {
    // Find completed sprints and restart them to pending status
    List<TaskSprint> completedSprintDb = sprintsDb.stream()
        .filter(x -> x.getStatus().isCompleted()).toList();
    if (isNotEmpty(completedSprintDb)) {
      for (TaskSprint sprint : completedSprintDb) {
        sprint.setStatus(TaskSprintStatus.PENDING);
      }
      taskSprintRepo.saveAll(completedSprintDb);
    }
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task sprint repository
   */
  @Override
  protected BaseRepository<TaskSprint, Long> getRepository() {
    return this.taskSprintRepo;
  }
}
