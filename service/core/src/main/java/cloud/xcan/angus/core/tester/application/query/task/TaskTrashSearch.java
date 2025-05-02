package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskTrashSearch {

  Page<TaskTrash> search(Set<SearchCriteria> criteria, Pageable pageable, Class<TaskTrash> clz,
      String... matches);

}




