package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisCaseSearch {

  Page<ApisCaseInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisCaseInfo> clz, String... matches);

}




