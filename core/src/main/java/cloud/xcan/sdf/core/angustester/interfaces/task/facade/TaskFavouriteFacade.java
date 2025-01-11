package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.favorite.TaskFavouriteFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.favorite.TaskFavouriteDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskFavouriteFacade {

  IdKey<Long, Object> add(Long taskId);

  void cancel(Long taskId);

  void cancelAll(Long projectId);

  PageResult<TaskFavouriteDetailVo> search(TaskFavouriteFindDto dto);

  Long count(Long projectId);

}
