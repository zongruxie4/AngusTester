package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisDesignSearch {

  Page<ApisDesignInfo> search(Set<SearchCriteria> criteria, PageRequest pageable,
      String... matches);
}
