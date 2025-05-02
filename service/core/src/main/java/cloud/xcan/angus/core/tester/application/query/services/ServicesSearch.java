package cloud.xcan.angus.core.tester.application.query.services;

import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicesSearch {

  Page<Services> search(Set<SearchCriteria> criteria, Pageable pageable, String... matches);

}




