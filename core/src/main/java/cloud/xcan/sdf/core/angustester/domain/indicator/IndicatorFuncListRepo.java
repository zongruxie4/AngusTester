package cloud.xcan.sdf.core.angustester.domain.indicator;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import java.util.Set;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface IndicatorFuncListRepo extends CustomBaseRepository<IndicatorFunc> {

  String getReturnFieldsCondition(Set<SearchCriteria> criteria, Object[] params);
}
