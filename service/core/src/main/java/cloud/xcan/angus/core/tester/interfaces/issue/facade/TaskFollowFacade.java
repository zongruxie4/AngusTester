package cloud.xcan.angus.core.tester.interfaces.issue.facade;

import cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.follow.TaskFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.follow.TaskFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskFollowFacade {

  IdKey<Long, Object> add(Long taskId);

  void cancel(Long taskId);

  void cancelAll(Long projectId);

  PageResult<TaskFollowDetailVo> list(TaskFollowFindDto dto);

  Long count(Long projectId);

}
