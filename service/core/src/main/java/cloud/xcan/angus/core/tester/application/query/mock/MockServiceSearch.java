package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MockServiceSearch {

  Page<MockServiceInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<MockServiceInfo> clz, String... matches);

}




