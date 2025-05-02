package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecTestCaseResultRepo extends BaseRepository<ExecTestCaseResult, Long> {

  List<ExecTestCaseResult> findByCaseIdIn(Collection<Long> caseIds);

  ExecTestCaseResult findByCaseId(Long caseId);

  List<ExecTestCaseResult> findByApisId(Long apisId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_test_case_result WHERE apis_id = ?1 AND case_id NOT IN ?2", nativeQuery = true)
  void deleteByApisIdAndIdNotIn(Long apisId, Collection<Long> caseIds);

}
