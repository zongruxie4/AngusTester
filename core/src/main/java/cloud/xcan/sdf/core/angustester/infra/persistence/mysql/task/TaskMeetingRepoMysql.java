package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.task;

import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingRepo;
import org.springframework.stereotype.Repository;

@Repository("taskMeetingRepo")
public interface TaskMeetingRepoMysql extends TaskMeetingRepo {

}
