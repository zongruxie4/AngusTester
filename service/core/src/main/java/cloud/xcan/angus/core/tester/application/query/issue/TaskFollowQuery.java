package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.core.tester.domain.issue.follow.TaskFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskFollowQuery {

  Page<TaskFollowP> list(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




