package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.auth;

import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ReportAuthCurrentVo {

  private boolean reportAuthFlag;

  private Set<ReportPermission> permissions;
}
