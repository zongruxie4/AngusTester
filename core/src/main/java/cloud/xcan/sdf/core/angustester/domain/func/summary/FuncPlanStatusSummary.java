package cloud.xcan.sdf.core.angustester.domain.func.summary;

import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FuncPlanStatusSummary {

  private FuncPlanStatus status;

  private long total;

}
