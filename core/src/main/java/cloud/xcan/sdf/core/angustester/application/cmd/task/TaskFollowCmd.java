package cloud.xcan.sdf.core.angustester.application.cmd.task;

import cloud.xcan.sdf.core.angustester.domain.task.follow.TaskFollow;
import cloud.xcan.sdf.spec.experimental.IdKey;

public interface TaskFollowCmd {

  IdKey<Long, Object> add(TaskFollow follow);

  void cancel(Long id);

  void cancelAll(Long projectId);

}




