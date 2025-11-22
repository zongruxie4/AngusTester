package cloud.xcan.angus.core.tester.domain.report.auth;

import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ReportAuthCurrent {

  private boolean reportAuth;

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
