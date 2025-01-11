package cloud.xcan.sdf.core.angustester.application.cmd.task;

import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskMeetingCmd {

  IdKey<Long, Object> add(TaskMeeting meeting);

  void update(TaskMeeting meeting);

  IdKey<Long, Object> replace(TaskMeeting meeting);

  void delete(Long id);
}
