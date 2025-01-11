package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnplannedWorkCount implements UnplannedWorkCountBase {

  @ApiModelProperty(value = "Total number of tasks or cases, Ignoring cancel status tasks or cases")
  protected long totalNum;
  @ApiModelProperty(value = "Total evaluate workload, Ignoring cancel status tasks or cases")
  protected double totalWorkload = 0d;

  // Unplanned(exclude bug)
  @ApiModelProperty(value = "The number of unplanned work")
  protected long unplannedNum;
  @ApiModelProperty(value = "The rate of unplanned work")
  protected double unplannedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed work")
  protected long unplannedCompletedNum;
  @ApiModelProperty(value = "The rate of unplanned and completed work")
  protected double unplannedCompletedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned workload")
  protected double unplannedWorkload = 0d;
  @ApiModelProperty(value = "The rate of unplanned workload")
  protected double unplannedWorkloadRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed workload")
  protected double unplannedWorkloadCompleted = 0d;
  @ApiModelProperty(value = "The rate of unplanned and completed workload")
  protected double unplannedWorkloadCompletedRate = 0d;

  @ApiModelProperty(value = "Daily processed average workload")
  protected double dailyProcessedWorkload;
  @ApiModelProperty("Unplanned workload completion time")
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
