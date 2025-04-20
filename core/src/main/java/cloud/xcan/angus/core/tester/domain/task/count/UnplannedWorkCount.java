package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnplannedWorkCount implements UnplannedWorkCountBase {

  @Schema(description = "Total number of tasks or cases, Ignoring cancel status tasks or cases")
  protected long totalNum;
  @Schema(description = "Total evaluate workload, Ignoring cancel status tasks or cases")
  protected double totalWorkload = 0d;

  // Unplanned(exclude bug)
  @Schema(description = "The number of unplanned work")
  protected long unplannedNum;
  @Schema(description = "The rate of unplanned work")
  protected double unplannedRate = 0d;
  @Schema(description = "The number of unplanned and completed work")
  protected long unplannedCompletedNum;
  @Schema(description = "The rate of unplanned and completed work")
  protected double unplannedCompletedRate = 0d;
  @Schema(description = "The number of unplanned workload")
  protected double unplannedWorkload = 0d;
  @Schema(description = "The rate of unplanned workload")
  protected double unplannedWorkloadRate = 0d;
  @Schema(description = "The number of unplanned and completed workload")
  protected double unplannedWorkloadCompleted = 0d;
  @Schema(description = "The rate of unplanned and completed workload")
  protected double unplannedWorkloadCompletedRate = 0d;

  @Schema(description = "Daily processed average workload")
  protected double dailyProcessedWorkload;
  @Schema(description="Unplanned workload completion time")
  protected double unplannedWorkloadProcessingTime;

  public double calcUnplannedRate() {
    return calcRate(unplannedNum, totalNum/* - validBugNum*/);
  }

  public double calcUnplannedCompletedRate() {
    return calcRate(unplannedCompletedNum, unplannedNum);
  }

  public double calcUnplannedWorkloadRate() {
    return calcRate(unplannedWorkload, totalWorkload);
  }

  public double calcUnplannedWorkloadCompletedRate() {
    return calcRate(unplannedWorkloadCompleted, unplannedWorkload);
  }
}
