package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskFollowQuery {

  Page<TaskFollowP> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




