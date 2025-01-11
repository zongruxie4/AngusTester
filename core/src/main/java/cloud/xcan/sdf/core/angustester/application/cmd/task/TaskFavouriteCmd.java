package cloud.xcan.sdf.core.angustester.application.cmd.task;

import cloud.xcan.sdf.core.angustester.domain.task.favorite.TaskFavourite;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskFavouriteCmd {

  IdKey<Long, Object> add(TaskFavourite favorite);

  void cancel(Long id);

  void cancelAll(Long projectId);
}




