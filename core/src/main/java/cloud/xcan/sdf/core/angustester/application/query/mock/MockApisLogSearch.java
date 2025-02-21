package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface MockApisLogSearch {

  Page<MockApisLogInfo> search(Long mockServiceId, Set<SearchCriteria> criteria,
      PageRequest pageable, Class<MockApisLogInfo> clz, String[] matchFields);
}
