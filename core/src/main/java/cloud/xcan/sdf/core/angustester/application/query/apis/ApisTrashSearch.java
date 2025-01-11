package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApisTrashSearch {

  Page<ApisTrash> search(Set<SearchCriteria> criterias, Pageable pageable,
      Class<ApisTrash> clz, String... matches);

}




