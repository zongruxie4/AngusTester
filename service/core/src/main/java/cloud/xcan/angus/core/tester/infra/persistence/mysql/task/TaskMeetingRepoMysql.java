package cloud.xcan.angus.core.tester.infra.persistence.mysql.task;

import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingRepo;
import org.springframework.stereotype.Repository;

@Repository("taskMeetingRepo")
public interface TaskMeetingRepoMysql extends TaskMeetingRepo {

}
