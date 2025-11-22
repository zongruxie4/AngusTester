package cloud.xcan.angus.core.tester.interfaces.report.facade.vo.auth;

import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ReportAuthCurrentVo {

  private boolean reportAuth;

  private Set<ReportPermission> permissions;
}
