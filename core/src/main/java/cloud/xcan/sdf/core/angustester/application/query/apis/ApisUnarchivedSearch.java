package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.unarchived.ApisUnarchived;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisUnarchivedSearch {

  Page<ApisUnarchived> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisUnarchived> apisClass, String... matches);

}




