package cloud.xcan.sdf.core.angustester.application.query.data;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DatasourceSearch {

  Page<Datasource> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<Datasource> mockDatasourceClass, String... matches);

}




