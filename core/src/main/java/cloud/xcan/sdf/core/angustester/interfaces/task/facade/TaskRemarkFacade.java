package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.remark.TaskRemarkFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.remark.TaskRemarkVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

/**
 * @author xiaolong.liu
 */
public interface TaskRemarkFacade {

  IdKey<Long, Object> add(TaskRemarkAddDto dto);

  void delete(Long id);

  PageResult<TaskRemarkVo> list(TaskRemarkFindDto dto);
}
