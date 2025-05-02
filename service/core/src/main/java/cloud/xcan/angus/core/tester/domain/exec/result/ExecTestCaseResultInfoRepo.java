package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.api.commonlink.exec.result.ExecTestCaseResultInfo;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecTestCaseResultInfoRepo extends BaseRepository<ExecTestCaseResultInfo, Long> {

  List<ExecTestCaseResultInfo> findByApisId(Long apiId);

  List<ExecTestCaseResultInfo> findByApisIdIn(Collection<Long> apiIds);

}
