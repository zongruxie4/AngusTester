package cloud.xcan.angus.core.tester.interfaces.task.facade;

import cloud.xcan.angus.core.tester.interfaces.task.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskFollowFacade {

  IdKey<Long, Object> add(Long taskId);

  void cancel(Long taskId);

  void cancelAll(Long projectId);

  PageResult<TaskFollowDetailVo> list(TaskFollowFindDto dto);

  Long count(Long projectId);

}
