package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.data.dataset.Dataset;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatasetSearch {

  Page<Dataset> search(Set<SearchCriteria> criterias, Pageable pageable, Class<Dataset> clz,
      String... matches);

}




