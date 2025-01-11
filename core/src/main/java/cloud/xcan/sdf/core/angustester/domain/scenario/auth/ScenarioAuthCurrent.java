package cloud.xcan.sdf.core.angustester.domain.scenario.auth;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioAuthCurrent {

  private boolean scenarioAuthFlag;

  private LinkedHashSet<ScenarioPermission> permissions;

  public void addPermissions(Collection<ScenarioPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }

}
