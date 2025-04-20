package cloud.xcan.angus.core.tester.interfaces.task.facade;

import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

/**
 * @author XiaoLong Liu
 */public interface TaskRemarkFacade {

  IdKey<Long, Object> add(TaskRemarkAddDto dto);

  void delete(Long id);

  PageResult<TaskRemarkVo> list(TaskRemarkFindDto dto);
}
