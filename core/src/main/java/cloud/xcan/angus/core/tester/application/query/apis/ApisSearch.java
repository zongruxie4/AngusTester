package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisSearch {

  Page<ApisBasicInfo> searchByServiceId(Long serviceId, Set<SearchCriteria> criteria,
      Pageable pageable, String... matches);

  Page<ApisBasicInfo> search(Set<SearchCriteria> criteria, Pageable pageable, String... matches);
}




