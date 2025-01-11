package cloud.xcan.sdf.core.angustester.application.query.services;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicesSearch {

  Page<Services> search(Set<SearchCriteria> criterias, Pageable pageable, String... matches);

}




