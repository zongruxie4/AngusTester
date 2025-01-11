package cloud.xcan.sdf.core.angustester.domain.script.auth;

import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptAuthCurrent {

  private boolean scriptAuthFlag;

  private LinkedHashSet<ScriptPermission> permissions;

  public void addPermissions(Collection<ScriptPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
