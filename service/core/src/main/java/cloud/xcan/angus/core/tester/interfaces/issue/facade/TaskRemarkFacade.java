package cloud.xcan.angus.core.tester.interfaces.issue.facade;

import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskRemarkFacade {

  IdKey<Long, Object> add(TaskRemarkAddDto dto);

  void delete(Long id);

  PageResult<TaskRemarkVo> list(TaskRemarkFindDto dto);
}
