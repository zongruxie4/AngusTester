package cloud.xcan.sdf.core.angustester.domain.task.sprint.auth;

import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintAuthCurrent {

  private boolean taskSprintAuthFlag;

  private LinkedHashSet<TaskSprintPermission> permissions;

  public void addPermissions(Collection<TaskSprintPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }

}
