package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.core.tester.domain.task.follow.TaskFollow;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TaskFollowCmd {

  IdKey<Long, Object> add(TaskFollow follow);

  void cancel(Long id);

  void cancelAll(Long projectId);

}




