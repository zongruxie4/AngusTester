package cloud.xcan.angus.core.tester.application.cmd.task;

import cloud.xcan.angus.core.tester.domain.task.trash.TaskTrash;
import java.util.List;

public interface TaskTrashCmd {

  void add0(List<TaskTrash> trashes);

  void clear(Long id);

  void clearAll(Long projectId);

  void back(Long id);

  void backAll(Long projectId);

}
