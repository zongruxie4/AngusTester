package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncTrashSearch {

  Page<FuncTrash> search(Set<SearchCriteria> criterias, Pageable pageable, Class<FuncTrash> clz,
      String... matches);

}




