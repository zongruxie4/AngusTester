package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseScenarioRepo extends BaseRepository<FuncCaseScenario, Long> {

  List<FuncCaseScenario> findByCaseId(Long caseId);

  @Modifying
  @Query(value = "DELETE FROM func_case_scenario WHERE case_id = ?1 AND scenario_id IN ?2", nativeQuery = true)
  void deleteByCaseIdAndScenarioIdIn(Long caseId, Collection<Long> scenarioIds);

}
