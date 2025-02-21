package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisShareSearch {

  Page<ApisShare> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisShare> clz, String... matches);

}
