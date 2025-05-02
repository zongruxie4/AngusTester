package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecTestResultRepo extends BaseRepository<ExecTestResult, Long> {

  ExecTestResult findByScriptSourceIdAndScriptType(Long sourceId, ScriptType scriptType);

  List<ExecTestResult> findByScriptSourceId(Long sourceId);

  List<ExecTestResult> findByExecIdAndScriptType(Long execId, ScriptType scriptType);

  ExecTestResult findByExecId(Long id);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_test_result WHERE script_source_id = ?2 AND script_type = ?1", nativeQuery = true)
  void deleteByScriptTypeAndScriptSourceId(String value, Long scriptSourceId);

}
