package cloud.xcan.sdf.core.angustester.application.query.analysis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.analysis.Analysis;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface AnalysisSearch {

  Page<Analysis> search(Set<SearchCriteria> criterias, PageRequest pageable,
      Class<Analysis> clz, String... matches);


}
