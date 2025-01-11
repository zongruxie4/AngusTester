package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting.TaskMeetingUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskMeetingFacade {

  IdKey<Long, Object> add(TaskMeetingAddDto dto);

  void update(TaskMeetingUpdateDto dto);

  IdKey<Long, Object> replace(TaskMeetingReplaceDto dto);

  void delete(Long id);

  TaskMeetingDetailVo detail(Long id);

  PageResult<TaskMeetingVo> list(TaskMeetingFindDto dto);

  PageResult<TaskMeetingVo> search(TaskMeetingSearchDto dto);

}
