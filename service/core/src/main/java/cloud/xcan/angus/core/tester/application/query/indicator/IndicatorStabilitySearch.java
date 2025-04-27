package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IndicatorStabilitySearch {

  Page<IndicatorStability> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<IndicatorStability> clz);

}




