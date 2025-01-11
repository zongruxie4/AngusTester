package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskSprintSearch {

  Page<TaskSprint> search(Set<SearchCriteria> criterias, Pageable pageable, Class<TaskSprint> clz,
      String... matches);

}




