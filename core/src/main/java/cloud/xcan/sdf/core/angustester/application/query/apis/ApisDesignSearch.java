package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisDesignSearch {

  Page<ApisDesignInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      String... matches);
}
