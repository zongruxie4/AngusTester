package cloud.xcan.angus.core.tester.interfaces.issue.facade;

import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskMeetingFacade {

  IdKey<Long, Object> add(TaskMeetingAddDto dto);

  void update(TaskMeetingUpdateDto dto);

  IdKey<Long, Object> replace(TaskMeetingReplaceDto dto);

  void delete(Long id);

  TaskMeetingDetailVo detail(Long id);

  PageResult<TaskMeetingVo> list(TaskMeetingFindDto dto);

}
