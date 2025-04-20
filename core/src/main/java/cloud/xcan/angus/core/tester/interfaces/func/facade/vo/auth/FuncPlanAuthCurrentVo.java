package cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth;

import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanAuthCurrentVo {

  private boolean funcPlanAuth;

  private Set<FuncPlanPermission> permissions;
}
