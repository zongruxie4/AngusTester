package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint.auth;

import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintAuthCurrentVo {

  private boolean taskSprintAuthFlag;

  private Set<TaskSprintPermission> permissions;
}
