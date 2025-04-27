package cloud.xcan.angus.core.tester.domain.func.summary;

import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
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
