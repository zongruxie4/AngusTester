package cloud.xcan.sdf.core.angustester.domain.task.meeting;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TaskMeetingSearchRepo extends CustomBaseRepository<TaskMeeting> {

}
