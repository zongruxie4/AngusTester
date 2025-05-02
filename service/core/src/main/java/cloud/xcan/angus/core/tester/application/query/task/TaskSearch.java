package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author XiaoLong Liu
 */public interface TaskSearch {

  Page<Task> search(boolean export, Set<SearchCriteria> criteria, PageRequest pageable,
      String... matches);
}
