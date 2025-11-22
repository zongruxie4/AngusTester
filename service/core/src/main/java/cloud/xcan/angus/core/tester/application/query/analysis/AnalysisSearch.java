package cloud.xcan.angus.core.tester.application.query.analysis;

import cloud.xcan.angus.core.tester.domain.analysis.Analysis;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface AnalysisSearch {

  Page<Analysis> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<Analysis> clz, String... matches);


}
