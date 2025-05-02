package cloud.xcan.angus.core.tester.application.query.indicator;

import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorPerfSearch {

  Page<IndicatorPerf> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<IndicatorPerf> clz);
}




