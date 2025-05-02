package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprint;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskSprintSearch {

  Page<TaskSprint> search(Set<SearchCriteria> criteria, Pageable pageable, Class<TaskSprint> clz,
      String... matches);

}




