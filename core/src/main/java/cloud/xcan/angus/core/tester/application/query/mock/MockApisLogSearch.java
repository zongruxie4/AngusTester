package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MockApisLogSearch {

  Page<MockApisLogInfo> search(Long mockServiceId, Set<SearchCriteria> criteria,
      PageRequest pageable, Class<MockApisLogInfo> clz, String[] matchFields);
}
