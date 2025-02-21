package cloud.xcan.sdf.core.angustester.application.query.indicator;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface IndicatorPerfSearch {

  Page<IndicatorPerf> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<IndicatorPerf> clz);
}




