package cloud.xcan.sdf.core.angustester.application.query.indicator;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorFuncSearch {

  Page<IndicatorFunc> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<IndicatorFunc> clz);
}




