package cloud.xcan.angus.core.tester.application.cmd.issue.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskTrashCmd;
import cloud.xcan.angus.core.tester.application.query.issue.TaskTrashQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.issue.sprint.auth.TaskSprintAuthRepo;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.issue.trash.TaskTrashRepo;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of task trash command operations for deleted item management.
 *
 * <p>This class provides functionality for managing deleted tasks and sprints
 * in the trash system, including restoration and permanent deletion.</p>
 *
 * <p>It handles the complete lifecycle of trash items from creation
 * to restoration or permanent deletion, including cascade operations.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Trash item management (tasks and sprints)</li>
 *   <li>Individual and bulk restoration operations</li>
 *   <li>Individual and bulk permanent deletion</li>
 *   <li>Cascade operations for related items</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
@Service
public class TaskTrashCmdImpl extends CommCmd<TaskTrash, Long> implements TaskTrashCmd {

  @Resource
  private TaskTrashRepo taskTrashRepo;
  @Resource
  private TaskTrashQuery taskTrashQuery;
  @Resource
  private TaskRepo taskRepo;
  @Resource
  private TaskInfoRepo taskInfoRepo;
  @Resource
  private TaskCmd taskCmd;
  @Resource
  private TaskSprintRepo taskSprintRepo;
  @Resource
  private TaskSprintCmd taskSprintCmd;
  @Resource
  private TaskSprintAuthRepo taskSprintAuthRepo;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds trash items without validation (internal use).
   *
   * <p>This method performs bulk insertion of trash items without
   * validation checks. It is intended for internal use only.</p>
   *
   * @param trashes the list of trash items to add
   */
  @Override
  public void add0(List<TaskTrash> trashes) {
    batchInsert0(trashes);
  }

  /**
   * Permanently deletes a trash item with cascade cleanup.
   *
   * <p>This method permanently removes a trash item and all its
   * associated data after verifying user permissions.</p>
   *
   * @param id the trash item ID to permanently delete
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      TaskTrash trashDb;

      @Override
      protected void checkParams() {
        // Verify trash item exists and user has permission to clear it
        trashDb = taskTrashQuery.findMyTrashForBiz(id, "CLEAR");
      }

      @Override
      protected Void process() {
        // Delete trash
        taskTrashRepo.deleteById(id);

        // Delete association data
        deleteAssociation(singletonList(trashDb));

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  /**
   * Permanently deletes all trash items for a project with cascade cleanup.
   *
   * <p>This method permanently removes all trash items for a project
   * and all their associated data.</p>
   *
   * @param projectId the project ID to clear all trash for (null for all projects)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        // Find existed trash
        List<TaskTrash> trashDbs = getAllTrashesByProject(projectId);

        if (isEmpty(trashDbs)) {
          return null;
        }

        // Delete all trash
        taskTrashRepo.deleteByTargetIdIn(trashDbs.stream().map(TaskTrash::getTargetId).collect(
            Collectors.toList()));

        // Delete association data
        deleteAssociation(trashDbs);

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  /**
   * Restores a trash item with cascade restoration.
   *
   * <p>This method restores a trash item (task or sprint) and all its
   * associated data after verifying user permissions.</p>
   *
   * <p>The method logs restoration activities for audit purposes.</p>
   *
   * @param id the trash item ID to restore
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      TaskTrash trashDb;

      @Override
      protected void checkParams() {
        // Verify trash item exists and user has permission to restore it
        trashDb = taskTrashQuery.findMyTrashForBiz(id, "BACK");
      }

      @Override
      protected Void process() {

        // Bach sprint and task
        if (trashDb.getTargetType().isSprint()) {
          backSprint(trashDb.getTargetId(), true);
        } else if (trashDb.getTargetType().isTask()) {
          backTasks(trashDb.getTargetId());
        }
        return null;
      }

      private void backTasks(Long taskId) {
        // Cascade recovery of current task
        List<Long> taskIds = List.of(taskId);
        taskRepo.updateToUndeletedStatusByIdIn(taskIds);

        Optional<TaskInfo> taskDb = taskInfoRepo.find0ById(trashDb.getTargetId());
        taskDb.ifPresent(funcCaseInfo -> backSprint(funcCaseInfo.getSprintId(), false));

        // Delete associated task trash
        taskTrashRepo.deleteByTargetIdIn(taskIds);

        // Log task restoration activity
        activityCmd.add(toActivity(TASK, trashDb, ActivityType.BACK, trashDb.getName()));
      }

      private void backSprint(Long sprintId, boolean saveActivity) {
        // Cascade recovery of current sprint
        List<Long> sprintIds = List.of(sprintId);
        taskSprintRepo.updateToUndeletedStatusByIdIn(sprintIds);

        // Delete associated sprint trash
        taskTrashRepo.deleteByTargetIdIn(sprintIds);

        // Update task parent sprint delete status
        taskRepo.updateSprintDeleteStatusBySprint(sprintIds, false);

        // Log sprint restoration activity
        if (saveActivity) {
          activityCmd.add(toActivity(TASK_SPRINT, trashDb, ActivityType.BACK, trashDb.getName()));
        }
      }
    }.execute();
  }

  /**
   * Restores all trash items for a project with cascade restoration.
   *
   * <p>This method restores all trash items for a project and all their
   * associated data, handling both tasks and sprints.</p>
   *
   * @param projectId the project ID to restore all trash for (null for all projects)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<TaskTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isNotEmpty(allTrashes)) {
          List<Long> sprintIds = allTrashes.stream().filter(d -> d.getTargetType().isSprint())
              .map(TaskTrash::getTargetId).toList();
          if (isNotEmpty(sprintIds)) {
            backAllSprint(sprintIds);
          }

          List<Long> taskIds = allTrashes.stream().filter(d -> d.getTargetType().isTask())
              .map(TaskTrash::getTargetId).toList();
          if (isNotEmpty(taskIds)) {
            backAllTask(taskIds);
          }
        }
        // No activity
        return null;
      }

      private void backAllTask(List<Long> taskIds) {
        // Restore tasks to undeleted status
        taskRepo.updateToUndeletedStatusByIdIn(taskIds);

        // Delete task trash entries
        taskTrashRepo.deleteByTargetIdIn(taskIds);

        // Cascade recovery of parent sprints
        List<Long> sprintIds = taskInfoRepo.findAll0SprintIdsByIdIn(taskIds);
        if (isNotEmpty(sprintIds)) {
          backAllSprint(sprintIds);
        }
      }

      private void backAllSprint(List<Long> sprintIds) {
        // Restore sprints to undeleted status
        taskSprintRepo.updateToUndeletedStatusByIdIn(sprintIds);

        // Update task sprint delete status
        taskRepo.updateSprintDeleteStatusBySprint(sprintIds, false);

        // Delete sprint trash entries
        taskTrashRepo.deleteByTargetIdIn(sprintIds);
      }
    }.execute();
  }

  private void deleteAssociation(List<TaskTrash> trashes) {
    List<Long> allTaskIds = new ArrayList<>();

    List<Long> sprintIds = trashes.stream().filter(d -> d.getTargetType().isSprint())
        .map(TaskTrash::getTargetId).toList();
    if (isNotEmpty(sprintIds)) {
      List<Long> sprintCaseIds = taskInfoRepo.findAll0SprintIdsByIdIn(sprintIds);
      if (isNotEmpty(sprintCaseIds)) {
        allTaskIds.addAll(sprintCaseIds);
      }

      taskSprintCmd.delete0(sprintIds);
      taskSprintAuthRepo.deleteBySprintIdIn(sprintIds);
      taskTrashRepo.deleteByTargetIdIn(sprintIds);
    }

    List<Long> taskIds = trashes.stream().filter(d -> d.getTargetType().isTask())
        .map(TaskTrash::getTargetId).toList();
    if (isNotEmpty(taskIds)) {
      allTaskIds.addAll(taskIds);
    }

    if (isNotEmpty(allTaskIds)) {
      taskCmd.delete0(allTaskIds);
      taskTrashRepo.deleteByTargetIdIn(allTaskIds);
    }
  }

  private List<TaskTrash> getAllTrashesByProject(Long projectId) {
    Long currentUserId = getUserId();
    List<TaskTrash> trashDbs;
    if (isNull(projectId)) {
      trashDbs = isAdmin() ? taskTrashRepo.findAll()
          : taskTrashRepo.findByCreatedBy(currentUserId);
    } else {
      trashDbs = isAdmin() ? taskTrashRepo.findByProjectId(projectId)
          : taskTrashRepo.findByProjectIdAndCreatedBy(projectId, currentUserId);
    }
    return trashDbs;
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task trash repository
   */
  @Override
  protected BaseRepository<TaskTrash, Long> getRepository() {
    return this.taskTrashRepo;
  }
}
