package cloud.xcan.angus.core.tester.domain.test.baseline;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineCaseRepo extends BaseRepository<FuncBaselineCase, Long> {

  List<FuncBaselineCase> findByCaseId(Long caseId);

  @Query(value = "SELECT case_id FROM func_baseline_case WHERE plan_id = ?1 AND baseline_id = ?2 ", nativeQuery = true)
  Set<Long> findCaseIdByPlanIdAndBaselineId(Long planId, Long baselineId);

  @Modifying
  @Query(value = "DELETE FROM func_baseline_case WHERE baseline_id IN ?1", nativeQuery = true)
  void deleteByBaselineIdIn(Collection<Long> ids);

}
