package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MockApisSearch {

  Page<MockApis> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<MockApis> clz, String... matches);

}




