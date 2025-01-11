package cloud.xcan.sdf.core.angustester.domain.report.auth;

import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ReportAuthCurrent {

  private boolean reportAuthFlag;

  private LinkedHashSet<ReportPermission> permissions;

  public void addPermissions(Collection<ReportPermission> permissions0) {
    if (ObjectUtils.isEmpty(permissions0)) {
      return;
    }
    if (permissions == null) {
      permissions = new LinkedHashSet<>();
    }
    permissions.addAll(permissions0);
  }
}
