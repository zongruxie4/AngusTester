package cloud.xcan.angus.core.tester.domain.services.auth;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServicesAuthCurrent {

  private boolean serviceAuth;

  private LinkedHashSet<ServicesPermission> permissions;

  public void addPermissions(Collection<ServicesPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
