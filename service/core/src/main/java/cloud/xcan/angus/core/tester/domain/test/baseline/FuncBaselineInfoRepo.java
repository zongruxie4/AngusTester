package cloud.xcan.angus.core.tester.domain.test.baseline;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineInfoRepo extends BaseRepository<FuncBaselineInfo, Long> {


}
