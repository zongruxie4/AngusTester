package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MockServiceSearch {

  Page<MockServiceInfo> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<MockServiceInfo> clz, String... matches);

}




