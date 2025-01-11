package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth;

import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanAuthCurrentVo {

  private boolean funcPlanAuthFlag;

  private Set<FuncPlanPermission> permissions;
}
