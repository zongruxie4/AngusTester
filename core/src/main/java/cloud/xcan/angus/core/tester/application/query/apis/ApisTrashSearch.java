package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisTrashSearch {

  Page<ApisTrash> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ApisTrash> clz, String... matches);

}




