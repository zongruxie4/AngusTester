package cloud.xcan.angus.core.tester.domain.func.plan.auth;

import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanAuthCurrent {

  private boolean funcPlanAuth;

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
