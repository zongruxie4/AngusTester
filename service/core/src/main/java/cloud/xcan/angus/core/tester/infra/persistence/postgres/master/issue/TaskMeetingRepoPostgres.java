package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.issue;

import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingRepo;
import org.springframework.stereotype.Repository;

@Repository("taskMeetingRepo")
public interface TaskMeetingRepoPostgres extends TaskMeetingRepo {

}
