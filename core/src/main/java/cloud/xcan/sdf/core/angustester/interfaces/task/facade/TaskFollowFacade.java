package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskFollowFacade {

  IdKey<Long, Object> add(Long taskId);

  void cancel(Long taskId);

  void cancelAll(Long projectId);

  PageResult<TaskFollowDetailVo> search(TaskFollowFindDto dto);

  Long count(Long projectId);

}
