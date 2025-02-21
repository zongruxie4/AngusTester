package cloud.xcan.sdf.core.angustester.domain.apis.auth;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthCurrent {

  private boolean projectAuthFlag;

  private boolean apisAuthFlag;

  private LinkedHashSet<ApiPermission> permissions;

  public void addPermissions(Collection<ApiPermission> permissions0) {
    if (isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
