package cloud.xcan.angus.core.tester.domain.script.auth;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptAuthCurrent {

  private boolean scriptAuth;

  private LinkedHashSet<ScriptPermission> permissions;

  public void addPermissions(Collection<ScriptPermission> permissions0) {
    if (isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
