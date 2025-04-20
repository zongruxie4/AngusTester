package cloud.xcan.angus.core.tester.domain.indicator;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorStabilityListRepo extends CustomBaseRepository<IndicatorStability> {

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);

}
