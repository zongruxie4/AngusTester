package cloud.xcan.angus.core.tester.domain.task.meeting;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskMeetingRepo extends BaseRepository<TaskMeeting, Long> {

  List<TaskMeeting> findBySprintId(Long sprintId);

}
