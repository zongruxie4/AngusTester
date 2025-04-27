package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorFuncSearch {

  Page<IndicatorFunc> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<IndicatorFunc> clz);
}




