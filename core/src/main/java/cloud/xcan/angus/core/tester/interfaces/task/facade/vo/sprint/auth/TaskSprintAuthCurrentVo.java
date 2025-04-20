package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.sprint.auth;

import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintAuthCurrentVo {

  private boolean taskSprintAuth;

  private Set<TaskSprintPermission> permissions;
}
