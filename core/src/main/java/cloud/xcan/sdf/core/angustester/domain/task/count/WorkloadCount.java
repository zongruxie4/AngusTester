package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkloadCount implements WorkloadCountBase{

  @ApiModelProperty(value = "Evaluate workload")
  protected double evalWorkload = 0d;
  @ApiModelProperty(value = "Actual workload")
  protected double actualWorkload = 0d;
  @ApiModelProperty(value = "Completed workload")
  protected double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;
  @ApiModelProperty(value = "Actual saving workload")
  protected double savingWorkload = 0d;
  @ApiModelProperty(value = "The rate of actual saving workload")
  protected double savingWorkloadRate = 0d;
  @ApiModelProperty(value = "Invalid workload")
  protected double invalidWorkload = 0d;
  @ApiModelProperty(value = "The rate of invalid workload")
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
