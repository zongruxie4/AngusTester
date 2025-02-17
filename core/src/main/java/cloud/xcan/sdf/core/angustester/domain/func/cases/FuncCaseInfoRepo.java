package cloud.xcan.sdf.core.angustester.domain.func.cases;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseInfoRepo extends BaseRepository<FuncCaseInfo, Long> {

  @Query(value = "SELECT * FROM func_case WHERE id = ?1", nativeQuery = true)
  Optional<FuncCaseInfo> find0ById(Long id);

  @Query(value = "SELECT * FROM func_case WHERE id in ?1", nativeQuery = true)
  List<FuncCaseInfo> findAll0ByIdIn(Collection<Long> ids);

  List<FuncCaseInfo> findByIdIn(Collection<Long> ids);

  @Query(value = "SELECT DISTINCT plan_id FROM func_case WHERE id in ?1", nativeQuery = true)
  List<Long> findAll0PlanIdByIdIn(Collection<Long> ids);

  List<FuncCaseInfo> findAllByNameInAndPlanId(Collection<String> names, long planId);

  @Query(value = "SELECT * FROM func_case WHERE plan_id = ?1", nativeQuery = true)
  List<FuncCaseInfo> findAllByPlanId(Long planId);

  @Query(value = "SELECT * FROM func_case WHERE plan_id = ?1 AND tester_id = ?2", nativeQuery = true)
  List<FuncCaseInfo> findAllByPlanIdAndTesterId(Long planId, Long userId);

  @Query(value = "SELECT * FROM func_case WHERE project_id = ?1 AND tester_id = ?2", nativeQuery = true)
  List<FuncCaseInfo> findAllByProjectIdAndTesterId(Long projectId, Long userId);

  @Query(value =
      "SELECT * FROM func_case WHERE overdue_flag = 0 AND ((deadline_date < now() AND test_result <> 'PASSED' AND test_result <> 'CANCELED') "
          + "OR (test_result_handle_date > deadline_date AND test_result = 'PASSED')) LIMIT ?1", nativeQuery = true)
  List<FuncCaseInfo> findIdByOverdue(Long count);

  List<FuncCaseInfo> findByPlanIdAndNameIn(Long planId, Collection<String> names);

  @Query(value = "SELECT * FROM func_case WHERE project_id = ?1 AND deleted_flag = 0 AND plan_deleted_flag = 0 ORDER BY created_date ASC LIMIT 1", nativeQuery = true)
  FuncCaseInfo findEarliestByProjectId(Long projectId);

  @Query(value = "SELECT * FROM func_case WHERE plan_id = ?1 AND deleted_flag = 0 AND plan_deleted_flag = 0 ORDER BY created_date ASC LIMIT 1", nativeQuery = true)
  FuncCaseInfo findEarliestByPlanId(Long planId);

  @Query(value = "SELECT * FROM func_case WHERE plan_id = ?1 AND deleted_flag = 0 AND plan_deleted_flag = 0 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  FuncCaseInfo findLeastByPlanId(Long planId);

  @Query(value = "SELECT * FROM func_case WHERE project_id = ?1 ORDER BY created_date DESC LIMIT 1", nativeQuery = true)
  FuncCaseInfo findLeastByProjectId(Long projectId);

  @Query(value = "SELECT * FROM func_case WHERE deadline_date < ?1 AND deadline_date >= ?2 AND test_result <> 'PASSED' AND test_result <> 'CANCELED' AND tester_id <> null AND deleted_flag = 0 AND plan_deleted_flag = 0 ORDER BY created_date ASC LIMIT ?3", nativeQuery = true)
  List<FuncCaseInfo> findWillOverdue(LocalDateTime now, LocalDateTime deadline, Long count);

}
