package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncTrashSearch {

  Page<FuncTrash> search(Set<SearchCriteria> criteria, Pageable pageable, Class<FuncTrash> clz,
      String... matches);

}




