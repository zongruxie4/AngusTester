package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBasicInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisSearch {

  Page<ApisBasicInfo> searchByServiceId(Long serviceId, Set<SearchCriteria> criterias,
      Pageable pageable, String... matches);

  Page<ApisBasicInfo> search(Set<SearchCriteria> criterias, Pageable pageable, String... matches);
}




