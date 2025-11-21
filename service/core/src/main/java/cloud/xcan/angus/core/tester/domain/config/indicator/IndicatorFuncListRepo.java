package cloud.xcan.angus.core.tester.domain.config.indicator;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorFuncListRepo extends CustomBaseRepository<IndicatorFunc> {

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);
}
