package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskFollowQuery {

  Page<TaskFollowP> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




