package cloud.xcan.angus.core.tester.application.query.issue;

import cloud.xcan.angus.core.tester.domain.issue.favorite.TaskFavouriteP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskFavouriteQuery {

  Page<TaskFavouriteP> list(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




