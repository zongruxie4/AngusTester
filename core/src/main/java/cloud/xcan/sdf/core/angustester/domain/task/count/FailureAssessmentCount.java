package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureAssessmentCount implements FailureAssessmentCountBase {

  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @ApiModelProperty(value = "Total evaluate workload, Ignoring cancel status tasks")
  protected double totalWorkload = 0d;

  @ApiModelProperty(value = "Total number of bugs, Ignoring cancel status bugs")
  protected long bugNum;

  // Failure (Bug Handling Times)
  @ApiModelProperty(value = "The total number of bugs handling times")
  protected long failureNum;
  @ApiModelProperty(value = "The total workload of bugs")
  protected double failureWorkload;
  @ApiModelProperty(value = "The number of bugs handling that exceed one")
  protected long oneTimeFailureNum;
  @ApiModelProperty(value = "The rate of bugs handling that exceed one")
  protected double oneTimeFailureRate = 0d;
  @ApiModelProperty(value = "The number of bugs handling that exceed two")
  protected long twoTimeFailureNum;
  @ApiModelProperty(value = "The rate of bugs handling that exceed two")
  protected double twoTimeFailureRate = 0d;
  @ApiModelProperty(value = "The number of completed failures")
  protected long failureCompletedNum;
  @ApiModelProperty(value = "The rate of completed failures")
  protected double failureCompletedRate;
  @ApiModelProperty(value = "The number of overdue failures")
  protected long failureOverdueNum;
  @ApiModelProperty(value = "The rate of overdue failures")
  protected double failureOverdueRate;

  @ApiModelProperty(value = "The total overdue time of completed failures")
  protected double failureTotalTime;
  @ApiModelProperty(value = "The average overdue time of completed failures")
  protected double failureAvgTime;
  @ApiModelProperty(value = "The min overdue time of completed failures")
  protected double failureMinTime;
  @ApiModelProperty(value = "The max overdue time of completed failures")
  protected double failureMaxTime;

  @ApiModelProperty(value = "The number of failures by different levels")
  protected LinkedHashMap<BugLevel, Integer> failureLevelCount = new LinkedHashMap<>();
  @ApiModelProperty(value = "The rate of failures by different levels")
  protected LinkedHashMap<BugLevel, Double> failureLevelRate = new LinkedHashMap<>();

  public double calcOneTimeFailureRate() {
    return calcRate(oneTimeFailureNum, failureNum);
  }

  public double calcTwoTimeFailureRate() {
    return calcRate(twoTimeFailureNum, failureNum);
  }

  public double calcFailureCompletedRate() {
    return calcRate(failureCompletedNum, failureNum);
  }

  public double calcFailureOverdueRate() {
    return calcRate(failureOverdueNum, failureNum);
  }
}
