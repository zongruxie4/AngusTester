package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.trash.TaskTrash;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskTrashSearch {

  Page<TaskTrash> search(Set<SearchCriteria> criteria, Pageable pageable, Class<TaskTrash> clz,
      String... matches);

}




