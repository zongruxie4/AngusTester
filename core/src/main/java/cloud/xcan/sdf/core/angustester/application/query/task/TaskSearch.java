package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
 */
public interface TaskSearch {

  Page<Task> search(boolean exportFlag, Set<SearchCriteria> criteria, PageRequest pageable,
      String... matches);
}
