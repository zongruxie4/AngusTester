package cloud.xcan.angus.core.tester.domain.issue.sprint.auth;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintPermission;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintAuthCurrent {

  private boolean taskSprintAuth;

  private LinkedHashSet<TaskSprintPermission> permissions;

  public void addPermissions(Collection<TaskSprintPermission> permissions0) {
    if (isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }

}
