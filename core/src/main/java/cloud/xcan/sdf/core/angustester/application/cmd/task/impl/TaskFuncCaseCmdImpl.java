package cloud.xcan.sdf.core.angustester.application.cmd.task.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskFuncCaseCmd;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCase;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

@Biz
public class TaskFuncCaseCmdImpl extends CommCmd<TaskFuncCase, Long> implements TaskFuncCaseCmd {

  @Resource
  private TaskFuncCaseRepo taskFuncCaseRepo;

  @Override
  public void addAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    if (isEmpty(taskIds) && isEmpty(caseIds)) {
      return;
    }

    List<TaskFuncCase> tfcsDb = taskFuncCaseRepo.findByTargetId(targetId);
    List<Long> assocTaskIdsDb = tfcsDb.stream().filter(TaskFuncCase::isAssocTask)
        .map(TaskFuncCase::getAssocTargetId).collect(Collectors.toList());
    List<Long> assocCaseIdsDb = tfcsDb.stream().filter(TaskFuncCase::isAssocFuncCase)
        .map(TaskFuncCase::getAssocTargetId).collect(Collectors.toList());

    List<TaskFuncCase> tfcs = new ArrayList<>();
    if (isNotEmpty(taskIds)) {
      tfcs.addAll(taskIds.stream().filter(x -> !assocTaskIdsDb.contains(x))
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.TASK)
          ).collect(Collectors.toList()));
    }

    if (isNotEmpty(caseIds)) {
      tfcs.addAll(caseIds.stream().filter(x -> !assocCaseIdsDb.contains(x))
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.FUNC_CASE)
          ).collect(Collectors.toList())
      );
    }
    if (isNotEmpty(tfcs)) {
      batchInsert0(tfcs);
    }
  }

  @Override
  public void replaceAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    replaceAssocTask0(targetType, targetId, taskIds);
    replaceAssocCase0(targetType, targetId, caseIds);
  }

  @Override
  public void updateAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    if (isNotEmpty(taskIds)) {
      replaceAssocTask0(targetType, targetId, taskIds);
    }

    if (isNotEmpty(caseIds)) {
      replaceAssocCase0(targetType, targetId, caseIds);
    }
  }

  @Override
  public void deleteAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds){
    if (isNotEmpty(taskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, taskIds);
      taskFuncCaseRepo.deleteByTargetIdInAndAssocTargetId(taskIds, targetId);
    }

    if (isNotEmpty(caseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, caseIds);
      taskFuncCaseRepo.deleteByTargetIdInAndAssocTargetId(caseIds, targetId);
    }
  }

  @Override
  public void replaceAssocTask0(CombinedTargetType targetType, Long targetId, Set<Long> taskIds) {
    if (isEmpty(taskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetType(targetId,
          CombinedTargetType.TASK.getValue());
      return;
    }

    List<Long> taskIdsDb = taskFuncCaseRepo.findByTargetIdAndAssocTargetType(
            targetId, CombinedTargetType.TASK).stream().map(TaskFuncCase::getAssocTargetId)
        .collect(Collectors.toList());

    List<Long> addTaskIds = new ArrayList<>(taskIds);
    addTaskIds.removeAll(taskIdsDb);
    if (isNotEmpty(addTaskIds)) {
      batchInsert0(addTaskIds.stream()
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.TASK)
          ).collect(Collectors.toList())
      );
    }

    List<Long> deleteTaskIds = new ArrayList<>(taskIdsDb);
    deleteTaskIds.removeAll(taskIds);
    if (isNotEmpty(deleteTaskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, deleteTaskIds);
    }
  }

  @Override
  public void replaceAssocCase0(CombinedTargetType targetType, Long targetId, Set<Long> caseIds) {
    if (isEmpty(caseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetType(targetId,
          CombinedTargetType.FUNC_CASE.getValue());
      return;
    }

    List<Long> caseIdsDb = taskFuncCaseRepo.findByTargetIdAndAssocTargetType(
            targetId, CombinedTargetType.FUNC_CASE).stream()
        .map(TaskFuncCase::getAssocTargetId).collect(Collectors.toList());

    List<Long> addCaseIds = new ArrayList<>(caseIds);
    addCaseIds.removeAll(caseIdsDb);
    if (isNotEmpty(addCaseIds)) {
      batchInsert0(addCaseIds.stream()
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.FUNC_CASE)
          ).collect(Collectors.toList())
      );
    }

    List<Long> deleteCaseIds = new ArrayList<>(caseIdsDb);
    deleteCaseIds.removeAll(caseIds);
    if (isNotEmpty(deleteCaseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, deleteCaseIds);
    }
  }

  @Override
  public void deleteByTargetIds(Collection<Long> targetIds) {
    taskFuncCaseRepo.deleteByTargetIdIn(targetIds);
  }

  @Override
  protected BaseRepository<TaskFuncCase, Long> getRepository() {
    return taskFuncCaseRepo;
  }
}
