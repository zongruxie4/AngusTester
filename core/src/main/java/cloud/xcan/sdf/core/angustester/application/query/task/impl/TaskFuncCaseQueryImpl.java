package cloud.xcan.sdf.core.angustester.application.query.task.impl;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskFuncCaseQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.task.cases.CaseTestHit;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCase;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCaseAssoc;
import cloud.xcan.sdf.core.angustester.domain.task.cases.TaskFuncCaseRepo;
import cloud.xcan.sdf.core.biz.Biz;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;

@Biz
public class TaskFuncCaseQueryImpl implements TaskFuncCaseQuery {

  @Resource
  private TaskFuncCaseRepo taskFuncCaseRepo;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private UserManager userManager;

  @Override
  public List<TaskFuncCase> findWideByTargetId(List<Long> targetIds) {
    return taskFuncCaseRepo.findWideByTargetIdIn(targetIds);
  }

  @Override
  public List<CaseTestHit> findCaseHitBugs(Set<Long> caseIds) {
    return taskFuncCaseRepo.findTestHitByCaseIdIn(caseIds);
  }

  /**
   * Set task assoc tasks and cases
   */
  @Override
  public void setAssocForTask(List<? extends TaskFuncCaseAssoc<?, ?>> tasks) {
    if (isNotEmpty(tasks)) {
      List<TaskFuncCase> tfcs = findWideByTargetId(
          tasks.stream().map(TaskFuncCaseAssoc::getId).collect(Collectors.toList()));
      if (isEmpty(tfcs)) {
        return;
      }

      List<Long> assocTaskIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocTask)
          .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
          .collect(Collectors.toList());
      Map<Long, TaskInfo> assocTaskInfoMap = taskInfoRepo.findByIdIn(assocTaskIds)
          .stream().collect(Collectors.toMap(TaskInfo::getId, x -> x));
      if (isNotEmpty(assocTaskInfoMap)) {
        for (TaskFuncCaseAssoc<?, ?> task : tasks) {
          List<Long> taskAssocTaskIds = tfcs.stream()
              .filter(x -> x.isTaskAssocTask() && x.getWideTargetIds().contains(task.getId()))
              .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
              .collect(Collectors.toList());
          taskAssocTaskIds.remove(task.getId());
          if (isNotEmpty(taskAssocTaskIds)) {
            task.setAssocTasks(assocTaskInfoMap.entrySet().stream()
                .filter(x -> taskAssocTaskIds.contains(x.getKey()))
                .map(Entry::getValue).collect(Collectors.toList()));
            // Set assignee name and avatar
            userManager.setUserNameAndAvatar(task.getAssocTasks(),
                "assigneeId", "assigneeName", "assigneeAvatar");
          }
        }
      }

      List<Long> assocCaseIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocCase)
          .map(TaskFuncCase::getAssocTargetId).collect(Collectors.toList());
      Map<Long, FuncCaseInfo> assocCaseInfoMap = funcCaseInfoRepo.findByIdIn(assocCaseIds)
          .stream().collect(Collectors.toMap(FuncCaseInfo::getId, x -> x));
      if (isNotEmpty(assocCaseInfoMap)) {
        for (TaskFuncCaseAssoc<?, ?> task : tasks) {
          List<Long> taskAssocCaseIds = tfcs.stream()
              .filter(x -> x.isTaskAssocCase() && x.getWideTargetIds().contains(task.getId()))
              .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
              .collect(Collectors.toList());
          taskAssocCaseIds.remove(task.getId());
          if (isNotEmpty(taskAssocCaseIds)) {
            task.setAssocCases(assocCaseInfoMap.entrySet().stream()
                .filter(x -> taskAssocCaseIds.contains(x.getKey()))
                .map(Entry::getValue).collect(Collectors.toList()));
            // Set tester name and avatar
            userManager.setUserNameAndAvatar(task.getAssocCases(), "testerId", "testerName",
                "testerAvatar");
          }
        }
      }
    }
  }

  /**
   * Set case assoc tasks and cases
   */
  @Override
  public void setAssocForCase(List<? extends TaskFuncCaseAssoc<?, ?>> cases) {
    if (isNotEmpty(cases)) {
      List<TaskFuncCase> tfcs = findWideByTargetId(
          cases.stream().map(TaskFuncCaseAssoc::getId).collect(Collectors.toList()));
      if (isEmpty(tfcs)) {
        return;
      }

      List<Long> assocTaskIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocCase)
          .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
          .collect(Collectors.toList());
      Map<Long, TaskInfo> assocTaskInfoMap = taskInfoRepo.findByIdIn(assocTaskIds)
          .stream().collect(Collectors.toMap(TaskInfo::getId, x -> x));
      if (isNotEmpty(assocTaskInfoMap)) {
        for (TaskFuncCaseAssoc<?, ?> case0 : cases) {
          List<Long> caseAssocTaskIds = tfcs.stream()
              .filter(x -> x.isTaskAssocCase() && x.getWideTargetIds().contains(case0.getId()))
              .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
              .collect(Collectors.toList());
          caseAssocTaskIds.remove(case0.getId());
          if (isNotEmpty(caseAssocTaskIds)) {
            case0.setAssocTasks(assocTaskInfoMap.entrySet().stream()
                .filter(x -> caseAssocTaskIds.contains(x.getKey()))
                .map(Entry::getValue).collect(Collectors.toList()));
            // Set assignee name and avatar
            userManager.setUserNameAndAvatar(case0.getAssocTasks(),
                "assigneeId", "assigneeName", "assigneeAvatar");
          }
        }
      }

      List<Long> assocCaseIds = tfcs.stream().filter(TaskFuncCase::isCaseAssocCase)
          .map(TaskFuncCase::getAssocTargetId).collect(Collectors.toList());
      Map<Long, FuncCaseInfo> assocCaseInfoMap = funcCaseInfoRepo.findByIdIn(assocCaseIds)
          .stream().collect(Collectors.toMap(FuncCaseInfo::getId, x -> x));
      if (isNotEmpty(assocCaseInfoMap)) {
        for (TaskFuncCaseAssoc<?, ?> case0 : cases) {
          List<Long> caseAssocCaseIds = tfcs.stream()
              .filter(x -> x.isCaseAssocCase() && x.getWideTargetIds().contains(case0.getId()))
              .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
              .collect(Collectors.toList());
          caseAssocCaseIds.remove(case0.getId());
          if (isNotEmpty(caseAssocCaseIds)) {
            case0.setAssocCases(assocCaseInfoMap.entrySet().stream()
                .filter(x -> caseAssocCaseIds.contains(x.getKey()))
                .map(Entry::getValue).collect(Collectors.toList()));
            // Set tester name and avatar
            userManager.setUserNameAndAvatar(case0.getAssocCases(), "testerId", "testerName",
                "testerAvatar");
          }
        }
      }
    }
  }
}
