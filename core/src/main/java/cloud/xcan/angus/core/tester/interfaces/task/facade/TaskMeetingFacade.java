package cloud.xcan.angus.core.tester.interfaces.task.facade;

import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingSearchDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskMeetingFacade {

  IdKey<Long, Object> add(TaskMeetingAddDto dto);

  void update(TaskMeetingUpdateDto dto);

  IdKey<Long, Object> replace(TaskMeetingReplaceDto dto);

  void delete(Long id);

  TaskMeetingDetailVo detail(Long id);

  PageResult<TaskMeetingVo> list(TaskMeetingFindDto dto);

  PageResult<TaskMeetingVo> search(TaskMeetingSearchDto dto);

}
