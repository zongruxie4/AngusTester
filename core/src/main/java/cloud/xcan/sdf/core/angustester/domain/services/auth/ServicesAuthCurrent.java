package cloud.xcan.sdf.core.angustester.domain.services.auth;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ServicesAuthCurrent {

  private boolean serviceAuthFlag;

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
