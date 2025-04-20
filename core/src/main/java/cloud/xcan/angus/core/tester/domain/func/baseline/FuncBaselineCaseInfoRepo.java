package cloud.xcan.angus.core.tester.domain.func.baseline;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineCaseInfoRepo extends BaseRepository<FuncBaselineCaseInfo, Long> {


}
