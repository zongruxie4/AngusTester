package cloud.xcan.sdf.core.angustester.application.query.task;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskMeetingSearch {

  Page<TaskMeeting> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<TaskMeeting> clz, String... matches);

}
