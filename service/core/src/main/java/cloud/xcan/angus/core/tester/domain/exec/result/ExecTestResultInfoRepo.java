package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.api.commonlink.exec.result.ExecTestResultInfo;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.model.script.ScriptSource;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecTestResultInfoRepo extends BaseRepository<ExecTestResultInfo, Long> {

  List<ExecTestResultInfo> findByScriptSourceIdInAndScriptSource(List<Long> enabledTestApiIds,
      ScriptSource scriptSource);
}
