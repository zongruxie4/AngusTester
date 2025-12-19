package cloud.xcan.angus.core.tester.application.query.issue.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.manager.UserManager;
import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.tester.application.query.issue.TaskFuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfoRepo;
import cloud.xcan.angus.core.tester.domain.issue.cases.CaseTestHit;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseAssoc;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseRepo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * Implementation of TaskFuncCaseQuery for task functional case association management and query
 * operations.
 * </p>
 * <p>
 * Provides methods for managing associations between tasks and functional cases, including test hit
 * analysis.
 * </p>
 */
@Service
public class TaskFuncCaseQueryImpl implements TaskFuncCaseQuery {

  @Resource
  private TaskFuncCaseRepo taskFuncCaseRepo;
  @Resource
  private TaskInfoRepo taskInfoRepo;
  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;
  @Resource
  private UserManager userManager;

  /**
   * <p>
   * Find wide task functional case associations by target IDs.
   * </p>
   * <p>
   * Retrieves task functional case associations that include the specified target IDs in their wide
   * target list.
   * </p>
   *
   * @param targetIds List of target IDs to search for
   * @return List of task functional case associations
   */
  @Override
  public List<TaskFuncCase> findWideByTargetId(List<Long> targetIds) {
    return taskFuncCaseRepo.findWideByTargetIdIn(targetIds);
  }

  /**
   * <p>
   * Find case test hit bugs for a set of case IDs.
   * </p>
   * <p>
   * Retrieves test hit information for functional cases, including bug detection results.
   * </p>
   *
   * @param caseIds Set of case IDs to find test hits for
   * @return List of case test hit information
   */
  @Override
  public List<CaseTestHit> findCaseHitBugs(Set<Long> caseIds) {
    return taskFuncCaseRepo.findTestHitByCaseIdIn(caseIds);
  }

  /**
   * <p>
   * Set associated tasks and cases for a list of task functional case associations.
   * </p>
   * <p>
   * Efficiently loads and sets associated tasks and cases for multiple task associations to avoid
   * N+1 query problems. Groups associations by type and sets user information (names and avatars)
   * for assignees and testers.
   * </p>
   *
   * @param tasks List of task functional case associations to set associations for
   */
  @Override
  public void setAssocForTask(List<? extends TaskFuncCaseAssoc<?, ?>> tasks) {
    if (isNotEmpty(tasks)) {
      List<TaskFuncCase> tfcs = findWideByTargetId(
          tasks.stream().map(TaskFuncCaseAssoc::getId).toList());
      if (isEmpty(tfcs)) {
        return;
      }

      List<Long> assocTaskIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocTask)
          .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
          .toList();
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
                .map(Entry::getValue).toList());
          }
        }
        // Set assignee name and avatar
        userManager.setUserNameAndAvatar(assocTaskInfoMap.values(),
            "assigneeId", "assigneeName", "assigneeAvatar");
      }

      List<Long> assocCaseIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocCase)
          .map(TaskFuncCase::getAssocTargetId).toList();
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
                .map(Entry::getValue).toList());
          }
        }
        // Set tester name and avatar
        userManager.setUserNameAndAvatar(assocCaseInfoMap.values(), "testerId", "testerName",
            "testerAvatar");
      }
    }
  }

  /**
   * <p>
   * Set associated tasks and cases for a list of case functional case associations.
   * </p>
   * <p>
   * Efficiently loads and sets associated tasks and cases for multiple case associations to avoid
   * N+1 query problems. Groups associations by type and sets user information (names and avatars)
   * for assignees and testers.
   * </p>
   *
   * @param cases List of case functional case associations to set associations for
   */
  @Override
  public void setAssocForCase(List<? extends TaskFuncCaseAssoc<?, ?>> cases) {
    if (isNotEmpty(cases)) {
      List<TaskFuncCase> tfcs = findWideByTargetId(
          cases.stream().map(TaskFuncCaseAssoc::getId).toList());
      if (isEmpty(tfcs)) {
        return;
      }

      List<Long> assocIds = tfcs.stream().filter(TaskFuncCase::isTaskAssocCase)
          .map(TaskFuncCase::getWideTargetIds).flatMap(Collection::stream).distinct()
          .toList();
      Map<Long, TaskInfo> assocTaskInfoMap = taskInfoRepo.findByIdIn(assocIds)
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
                .map(Entry::getValue).toList());
          }
        }
        // Set assignee name and avatar
        userManager.setUserNameAndAvatar(assocTaskInfoMap.values(),
            "assigneeId", "assigneeName", "assigneeAvatar");
      }

      List<Long> assocCaseIds = tfcs.stream().filter(TaskFuncCase::isCaseAssocCase)
          .map(TaskFuncCase::getAssocTargetId).toList();
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
                .map(Entry::getValue).toList());
          }
        }
        // Set tester name and avatar
        userManager.setUserNameAndAvatar(assocCaseInfoMap.values(), "testerId", "testerName",
            "testerAvatar");
      }
    }
  }
}
