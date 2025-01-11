package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisCaseSearch {

  Page<ApisCaseInfo> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ApisCaseInfo> clz, String... matches);

}




