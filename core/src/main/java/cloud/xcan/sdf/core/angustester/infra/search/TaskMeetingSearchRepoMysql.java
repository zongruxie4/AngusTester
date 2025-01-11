package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TaskMeetingSearchRepoMysql extends SimpleSearchRepository<TaskMeeting> implements
    TaskMeetingSearchRepo {

}

