package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthCurrentVo {

  private boolean projectAuth;

  private boolean apisAuth;

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
