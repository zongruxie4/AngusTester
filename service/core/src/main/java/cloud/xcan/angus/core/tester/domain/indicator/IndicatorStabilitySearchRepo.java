package cloud.xcan.angus.core.tester.domain.indicator;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorStabilitySearchRepo extends CustomBaseRepository<IndicatorStability> {


}
