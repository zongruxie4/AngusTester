package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingRepo;
import org.springframework.stereotype.Repository;

@Repository("taskMeetingRepo")
public interface TaskMeetingRepoMysql extends TaskMeetingRepo {

}
