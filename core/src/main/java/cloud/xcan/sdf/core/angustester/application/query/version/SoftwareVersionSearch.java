package cloud.xcan.sdf.core.angustester.application.query.version;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SoftwareVersionSearch {

  Page<SoftwareVersion> search(Set<SearchCriteria> criterias, Pageable pageable, Class<SoftwareVersion> clz,
      String... matches);

}




