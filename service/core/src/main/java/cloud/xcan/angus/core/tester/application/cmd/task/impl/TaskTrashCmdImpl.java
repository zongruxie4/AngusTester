package cloud.xcan.angus.core.tester.application.cmd.task.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK_SPRINT;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskSprintCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskTrashCmd;
import cloud.xcan.angus.core.tester.application.query.task.TaskTrashQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintRepo;
import cloud.xcan.angus.core.tester.domain.task.sprint.auth.TaskSprintAuthRepo;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrashRepo;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Biz
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

  @Override
  public void add0(List<TaskTrash> trashes) {
    batchInsert0(trashes);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      TaskTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      TaskTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
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
        // Cascade recovery current task
        List<Long> taskIds = List.of(taskId);
        taskRepo.updateToUndeletedStatusByIdIn(taskIds);

        Optional<TaskInfo> taskDb = taskInfoRepo.find0ById(trashDb.getTargetId());
        taskDb.ifPresent(funcCaseInfo -> backSprint(funcCaseInfo.getSprintId(), false));

        // Delete associated task trash
        taskTrashRepo.deleteByTargetIdIn(taskIds);

        // Add back task activity
        activityCmd.add(toActivity(TASK, trashDb, ActivityType.BACK, trashDb.getName()));
      }

      private void backSprint(Long sprintId, boolean saveActivity) {
        // Cascade recovery current sprint
        List<Long> sprintIds = List.of(sprintId);
        taskSprintRepo.updateToUndeletedStatusByIdIn(sprintIds);

        // Delete associated sprint trash
        taskTrashRepo.deleteByTargetIdIn(sprintIds);

        // Update case parent sprint delete status
        taskRepo.updateSprintDeleteStatusBySprint(sprintIds, false);

        // Add back sprint activity
        if (saveActivity) {
          activityCmd.add(toActivity(TASK_SPRINT, trashDb, ActivityType.BACK, trashDb.getName()));
        }
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        List<TaskTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isNotEmpty(allTrashes)) {
          List<Long> sprintIds = allTrashes.stream().filter(d -> d.getTargetType().isSprint())
              .map(TaskTrash::getTargetId).collect(Collectors.toList());
          if (isNotEmpty(sprintIds)) {
            backAllSprint(sprintIds);
          }

          List<Long> taskIds = allTrashes.stream().filter(d -> d.getTargetType().isTask())
              .map(TaskTrash::getTargetId).collect(Collectors.toList());
          if (isNotEmpty(taskIds)) {
            backAllTask(taskIds);
          }
        }
        // No activity
        return null;
      }

      private void backAllTask(List<Long> taskIds) {
        taskRepo.updateToUndeletedStatusByIdIn(taskIds);

        // Delete task trash
        taskTrashRepo.deleteByTargetIdIn(taskIds);

        // Cascade recovery parent sprints
        List<Long> sprintIds = taskInfoRepo.findAll0SprintIdsByIdIn(taskIds);
        if (isNotEmpty(sprintIds)) {
          backAllSprint(sprintIds);
        }
      }

      private void backAllSprint(List<Long> sprintIds) {
        // Update sprint delete flag
        taskSprintRepo.updateToUndeletedStatusByIdIn(sprintIds);

        // Update task sprint delete flag
        taskRepo.updateSprintDeleteStatusBySprint(sprintIds, false);

        // Delete sprint trash
        taskTrashRepo.deleteByTargetIdIn(sprintIds);
      }
    }.execute();
  }

  private void deleteAssociation(List<TaskTrash> trashes) {
    List<Long> allTaskIds = new ArrayList<>();

    List<Long> sprintIds = trashes.stream().filter(d -> d.getTargetType().isSprint())
        .map(TaskTrash::getTargetId).collect(Collectors.toList());
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
        .map(TaskTrash::getTargetId).collect(Collectors.toList());
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

  @Override
  protected BaseRepository<TaskTrash, Long> getRepository() {
    return this.taskTrashRepo;
  }
}
