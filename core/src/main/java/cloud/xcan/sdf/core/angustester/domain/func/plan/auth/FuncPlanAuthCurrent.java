package cloud.xcan.sdf.core.angustester.domain.func.plan.auth;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanAuthCurrent {

  private boolean funcPlanAuthFlag;

  private LinkedHashSet<FuncPlanPermission> permissions;

  public void addPermissions(Collection<FuncPlanPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
