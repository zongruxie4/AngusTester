package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskMeetingCmd {

  IdKey<Long, Object> add(TaskMeeting meeting);

  void update(TaskMeeting meeting);

  IdKey<Long, Object> replace(TaskMeeting meeting);

  void delete(Long id);
}
