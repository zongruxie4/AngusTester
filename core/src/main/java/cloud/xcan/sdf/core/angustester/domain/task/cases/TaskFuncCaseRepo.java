package cloud.xcan.sdf.core.angustester.domain.task.cases;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskFuncCaseRepo extends BaseRepository<TaskFuncCase, Long> {

  List<TaskFuncCase> findByTargetIdAndAssocTargetType(Long targetId,
      CombinedTargetType assocTargetType);

  @Query(value = "SELECT * FROM task_func_case WHERE target_id = ?1 OR assoc_target_id = ?1", nativeQuery = true)
  List<TaskFuncCase> findWideByTargetIdIn(Collection<Long> targetIds);

  List<TaskFuncCase> findByTargetId(Long targetId);

  @Query(value =
      "SELECT c.target_id caseId, t.id bugId, t.created_by bugCreatedBy FROM task_func_case c, task t"
          + " WHERE c.target_id IN ?1 AND c.target_type = 'FUNC_CASE' AND c.assoc_target_type = 'TASK' "
          + "  AND c.assoc_target_id = t.id AND t.task_type = 'BUG' AND t.`status` <> 'CANCELED' "
          + "UNION SELECT c.assoc_target_id caseId, t.id bugId, t.created_by bugCreatedBy FROM task_func_case c, task t "
          + " WHERE c.assoc_target_id IN ?1 AND c.assoc_target_type = 'FUNC_CASE' AND c.target_id = t.id "
          + " AND c.target_type = 'TASK' AND t.task_type = 'BUG' AND t.`status` <> 'CANCELED'", nativeQuery = true)
  List<CaseTestHit> findTestHitByCaseIdIn(Collection<Long> caseIds);

  @Modifying
  @Query(value = "DELETE FROM task_func_case WHERE target_id = ?1 AND assoc_target_type = ?2", nativeQuery = true)
  void deleteByTargetIdAndAssocTargetType(Long targetId, String assocTargetType);

  @Modifying
  @Query(value = "DELETE FROM task_func_case WHERE target_id = ?1 AND assoc_target_id IN ?2", nativeQuery = true)
  void deleteByTargetIdAndAssocTargetIdIn(Long targetId, Collection<Long> assocTargetIds);

  @Modifying
  @Query(value = "DELETE FROM task_func_case WHERE target_id IN ?1 AND assoc_target_id = ?2", nativeQuery = true)
  void deleteByTargetIdInAndAssocTargetId(Collection<Long> targetIds, Long assocTargetId);

  @Modifying
  @Query(value = "DELETE FROM task_func_case WHERE target_id IN ?1", nativeQuery = true)
  void deleteByTargetIdIn(Collection<Long> targetIds);

}
