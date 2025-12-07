package cloud.xcan.angus.core.tester.domain.issue.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkloadCount implements WorkloadCountBase {

  @Schema(description = "Evaluate workload")
  protected double evalWorkload = 0d;
  @Schema(description = "Actual workload")
  protected double actualWorkload = 0d;
  @Schema(description = "Completed workload")
  protected double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;
  @Schema(description = "Actual saving workload")
  protected double savingWorkload = 0d;
  @Schema(description = "The rate of actual saving workload")
  protected double savingWorkloadRate = 0d;
  @Schema(description = "Invalid workload")
  protected double invalidWorkload = 0d;
  @Schema(description = "The rate of invalid workload")
  protected double invalidWorkloadRate = 0d;

  public double calcCompletedWorkloadRate() {
    return calcRate(completedWorkload, evalWorkload);
  }

  public double calcSavingWorkloadRate() {
    return calcRate(savingWorkload, evalWorkload);
  }

  public double calcInvalidWorkloadRate() {
    return calcRate(invalidWorkload, evalWorkload);
  }
}
