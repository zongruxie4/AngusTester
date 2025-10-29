package cloud.xcan.angus.core.tester.application.cmd.issue;

import cloud.xcan.angus.core.tester.domain.issue.sprint.auth.TaskSprintAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Set;

public interface TaskSprintAuthCmd {

  IdKey<Long, Object> add(TaskSprintAuth auth);

  void replace(TaskSprintAuth auth);

  void enabled(Long sprintId, Boolean enabled);

  void delete(Long id);

  void addCreatorAuth(Long sprintId, Set<Long> creatorIds);

}




