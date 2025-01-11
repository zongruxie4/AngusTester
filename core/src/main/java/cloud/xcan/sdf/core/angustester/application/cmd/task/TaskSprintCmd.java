package cloud.xcan.sdf.core.angustester.application.cmd.task;

import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprint;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface TaskSprintCmd {

  IdKey<Long, Object> add(TaskSprint sprint);

  void update(TaskSprint sprint);

  IdKey<Long, Object> replace(TaskSprint sprint);

  void start(Long id);

  void block(Long id);

  void end(Long id);

  void move(Long id, Long projectId);

  IdKey<Long, Object> clone(Long id);

  void restart(HashSet<Long> ids);

  void reopen(HashSet<Long> ids);

  void delete(Long id);

  void delete0(List<Long> sprintIds);

}
