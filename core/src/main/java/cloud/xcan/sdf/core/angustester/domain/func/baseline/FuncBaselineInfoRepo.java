package cloud.xcan.sdf.core.angustester.domain.func.baseline;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncBaselineInfoRepo extends BaseRepository<FuncBaselineInfo, Long> {


}
