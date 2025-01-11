package cloud.xcan.sdf.core.angustester.domain.indicator;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorPerfSearchRepo extends CustomBaseRepository<IndicatorPerf> {

}
