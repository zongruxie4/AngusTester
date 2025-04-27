package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskMeetingSearch {

  Page<TaskMeeting> search(Set<SearchCriteria> criteria, PageRequest pageable,
      Class<TaskMeeting> clz, String... matches);

}
