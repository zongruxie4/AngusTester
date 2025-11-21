package cloud.xcan.angus.core.tester.domain.config.indicator;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorFuncSearchRepo extends CustomBaseRepository<IndicatorFunc> {

}
