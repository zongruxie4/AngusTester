package cloud.xcan.angus.core.tester.application.query.version;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoftwareVersionSearch {

  Page<SoftwareVersion> search(Set<SearchCriteria> criteria, Pageable pageable, Class<SoftwareVersion> clz,
      String... matches);

}




