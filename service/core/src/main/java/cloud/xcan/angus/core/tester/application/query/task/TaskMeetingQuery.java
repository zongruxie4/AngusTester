package cloud.xcan.angus.core.tester.application.query.task;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface TaskMeetingQuery {

  TaskMeeting detail(Long id);

  Page<TaskMeeting> list(GenericSpecification<TaskMeeting> spec, PageRequest pageable,
      boolean fullTextSearch, String[] match);

  List<TaskMeeting> findBySprintId(Long sprintId);

  TaskMeeting checkAndFind(Long id);

  List<TaskMeeting> getMeetingCreatedSummaries(Long projectId, Long sprintId,
      LocalDateTime createdDateStart, LocalDateTime createdDateEnd, AuthObjectType creatorOrgType,
      Long creatorOrgId);

}
