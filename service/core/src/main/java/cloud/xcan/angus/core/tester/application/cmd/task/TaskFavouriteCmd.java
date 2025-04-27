package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.core.tester.domain.task.favorite.TaskFavourite;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskFavouriteCmd {

  IdKey<Long, Object> add(TaskFavourite favorite);

  void cancel(Long id);

  void cancelAll(Long projectId);
}




