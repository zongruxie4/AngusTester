package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.task;

import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingRepo;
import org.springframework.stereotype.Repository;

@Repository("taskMeetingRepo")
public interface TaskMeetingRepoPostgres extends TaskMeetingRepo {

}
