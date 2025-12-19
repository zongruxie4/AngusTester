package cloud.xcan.angus.core.tester.application.cmd.issue.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskFuncCaseCmd;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCase;
import cloud.xcan.angus.core.tester.domain.issue.cases.TaskFuncCaseRepo;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Implementation of task functional case command operations for task-case associations.
 *
 * <p>This class provides functionality for managing associations between tasks
 * and functional cases, enabling bidirectional relationships.</p>
 *
 * <p>It handles the complete lifecycle of task-case associations from creation
 * to deletion, including bulk operations and relationship management.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Task-case association management</li>
 *   <li>Bulk association operations</li>
 *   <li>Association replacement and updates</li>
 *   <li>Bidirectional relationship handling</li>
 *   <li>Target-based association management</li>
 * </ul></p>
 */
@Service
public class TaskFuncCaseCmdImpl extends CommCmd<TaskFuncCase, Long> implements TaskFuncCaseCmd {

  @Resource
  private TaskFuncCaseRepo taskFuncCaseRepo;

  /**
   * Adds associations between tasks and functional cases.
   *
   * <p>This method creates new associations between the target and specified
   * tasks/cases, avoiding duplicate associations.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param taskIds    the set of task IDs to associate
   * @param caseIds    the set of case IDs to associate
   */
  @Override
  public void addAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    if (isEmpty(taskIds) && isEmpty(caseIds)) {
      return;
    }

    List<TaskFuncCase> tfcsDb = taskFuncCaseRepo.findByTargetId(targetId);
    List<Long> assocTaskIdsDb = tfcsDb.stream().filter(TaskFuncCase::isAssocTask)
        .map(TaskFuncCase::getAssocTargetId).toList();
    List<Long> assocCaseIdsDb = tfcsDb.stream().filter(TaskFuncCase::isAssocFuncCase)
        .map(TaskFuncCase::getAssocTargetId).toList();

    List<TaskFuncCase> tfcs = new ArrayList<>();
    if (isNotEmpty(taskIds)) {
      tfcs.addAll(taskIds.stream().filter(x -> !assocTaskIdsDb.contains(x))
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.TASK)
          ).toList());
    }

    if (isNotEmpty(caseIds)) {
      tfcs.addAll(caseIds.stream().filter(x -> !assocCaseIdsDb.contains(x))
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.FUNC_CASE)
          ).toList()
      );
    }
    if (isNotEmpty(tfcs)) {
      batchInsert0(tfcs);
    }
  }

  /**
   * Replaces all associations for a target with new task and case associations.
   *
   * <p>This method completely replaces existing associations with new ones,
   * removing old associations and creating new ones as needed.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param taskIds    the set of task IDs to associate
   * @param caseIds    the set of case IDs to associate
   */
  @Override
  public void replaceAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    replaceAssocTask0(targetType, targetId, taskIds);
    replaceAssocCase0(targetType, targetId, caseIds);
  }

  /**
   * Updates associations for a target, replacing only specified associations.
   *
   * <p>This method updates only the specified task or case associations,
   * leaving other associations unchanged.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param taskIds    the set of task IDs to associate (null to skip)
   * @param caseIds    the set of case IDs to associate (null to skip)
   */
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

  /**
   * Deletes specific associations between tasks and functional cases.
   *
   * <p>This method removes bidirectional associations between the target
   * and specified tasks/cases.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param taskIds    the set of task IDs to disassociate
   * @param caseIds    the set of case IDs to disassociate
   */
  @Override
  public void deleteAssoc(CombinedTargetType targetType, Long targetId,
      Set<Long> taskIds, Set<Long> caseIds) {
    if (isNotEmpty(taskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, taskIds);
      taskFuncCaseRepo.deleteByTargetIdInAndAssocTargetId(taskIds, targetId);
    }

    if (isNotEmpty(caseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, caseIds);
      taskFuncCaseRepo.deleteByTargetIdInAndAssocTargetId(caseIds, targetId);
    }
  }

  /**
   * Replaces task associations for a target (internal use).
   *
   * <p>This method completely replaces task associations for a target,
   * removing old associations and creating new ones as needed.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param taskIds    the set of task IDs to associate
   */
  @Override
  public void replaceAssocTask0(CombinedTargetType targetType, Long targetId, Set<Long> taskIds) {
    if (isEmpty(taskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetType(targetId,
          CombinedTargetType.TASK.getValue());
      return;
    }

    List<Long> taskIdsDb = taskFuncCaseRepo.findByTargetIdAndAssocTargetType(
            targetId, CombinedTargetType.TASK).stream().map(TaskFuncCase::getAssocTargetId)
        .toList();

    List<Long> addTaskIds = new ArrayList<>(taskIds);
    addTaskIds.removeAll(taskIdsDb);
    if (isNotEmpty(addTaskIds)) {
      batchInsert0(addTaskIds.stream()
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.TASK)
          ).toList()
      );
    }

    List<Long> deleteTaskIds = new ArrayList<>(taskIdsDb);
    deleteTaskIds.removeAll(taskIds);
    if (isNotEmpty(deleteTaskIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, deleteTaskIds);
    }
  }

  /**
   * Replaces case associations for a target (internal use).
   *
   * <p>This method completely replaces case associations for a target,
   * removing old associations and creating new ones as needed.</p>
   *
   * @param targetType the type of the target entity
   * @param targetId   the ID of the target entity
   * @param caseIds    the set of case IDs to associate
   */
  @Override
  public void replaceAssocCase0(CombinedTargetType targetType, Long targetId, Set<Long> caseIds) {
    if (isEmpty(caseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetType(targetId,
          CombinedTargetType.FUNC_CASE.getValue());
      return;
    }

    List<Long> caseIdsDb = taskFuncCaseRepo.findByTargetIdAndAssocTargetType(
            targetId, CombinedTargetType.FUNC_CASE).stream()
        .map(TaskFuncCase::getAssocTargetId).toList();

    List<Long> addCaseIds = new ArrayList<>(caseIds);
    addCaseIds.removeAll(caseIdsDb);
    if (isNotEmpty(addCaseIds)) {
      batchInsert0(addCaseIds.stream()
          .map(x -> new TaskFuncCase().setTargetId(targetId).setTargetType(targetType)
              .setAssocTargetId(x).setAssocTargetType(CombinedTargetType.FUNC_CASE)
          ).toList()
      );
    }

    List<Long> deleteCaseIds = new ArrayList<>(caseIdsDb);
    deleteCaseIds.removeAll(caseIds);
    if (isNotEmpty(deleteCaseIds)) {
      taskFuncCaseRepo.deleteByTargetIdAndAssocTargetIdIn(targetId, deleteCaseIds);
    }
  }

  /**
   * Deletes all associations for multiple targets.
   *
   * <p>This method removes all task-case associations for the specified targets.</p>
   *
   * @param targetIds the collection of target IDs to delete associations for
   */
  @Override
  public void deleteByTargetIds(Collection<Long> targetIds) {
    taskFuncCaseRepo.deleteByTargetIdIn(targetIds);
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the task functional case repository
   */
  @Override
  protected BaseRepository<TaskFuncCase, Long> getRepository() {
    return taskFuncCaseRepo;
  }
}
