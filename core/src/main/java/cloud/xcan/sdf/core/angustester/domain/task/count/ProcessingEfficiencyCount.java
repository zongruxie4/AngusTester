package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessingEfficiencyCount implements ProcessingEfficiencyCountBase {

  @ApiModelProperty(value = "Total number of work, Ignoring cancel status tasks")
  protected long totalNum;

  @ApiModelProperty(value = "Completed number")
  protected long completedNum; // Avoiding duplicate definitions
  @ApiModelProperty(value = "The rate of completed work, equal to the progress")
  protected double completedRate;
  @ApiModelProperty(value = "The number of tasks that the passed at one time")
  protected long oneTimePassedNum;
  @ApiModelProperty(value = "One-time pass rate")
  protected double oneTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of tasks that the passed at two time")
  protected long twoTimePassedNum;
  @ApiModelProperty(value = "Two-time pass rate")
  protected double twoTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of tasks that the passed not at one time")
  protected long oneTimeNotPassedNum;
  @ApiModelProperty(value = "One-time not pass rate")
  protected double oneTimeNotPassedRate = 0d;

  public double calcComplatedRate() {
    return calcRate(completedNum, totalNum);
  }

  public double calcOneTimePassedRate() {
    return calcRate(oneTimePassedNum, completedNum);
  }

  public double calcTwoTimePassedRate() {
    return calcRate(twoTimePassedNum, completedNum);
  }

  public double calcOneTimeNotPassedRate() {
    return calcRate(oneTimeNotPassedNum, completedNum);
  }

}
