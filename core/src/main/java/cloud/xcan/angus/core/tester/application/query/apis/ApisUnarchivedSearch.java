package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.unarchived.ApisUnarchived;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisUnarchivedSearch {

  Page<ApisUnarchived> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisUnarchived> apisClass, String... matches);

}




