package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MockApisSearch {

  Page<MockApis> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<MockApis> clz, String... matches);

}




