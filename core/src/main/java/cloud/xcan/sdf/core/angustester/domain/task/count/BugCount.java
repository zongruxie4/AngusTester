package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BugCount implements BugCountBase{

  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @ApiModelProperty(value = "Total evaluate workload, Ignoring cancel status tasks")
  protected double totalWorkload = 0d;

  // Submitted Bug by Creator(The vast majority of cases are submitted by testers)
  @ApiModelProperty(value = "Total number of bug type task")
  protected long bugNum;
  @ApiModelProperty(value = "Total rate of bug type task")
  protected double bugRate = 0d;
  @ApiModelProperty(value = "Total number of valid bug type task, Ignoring cancel status tasks")
  protected long validBugNum;
  @ApiModelProperty(value = "The rate of valid bug type task, Ignoring cancel status tasks")
  protected double validBugRate = 0d;
  @ApiModelProperty(value = "Total number of invalid bug type task")
  protected long invalidBugNum;
  @ApiModelProperty(value = "The rate of invalid bug type task")
  protected double invalidBugRate = 0d;
  @ApiModelProperty(value = "Total workload of bug type task, Ignoring cancel status tasks")
  protected double bugWorkload;
  @ApiModelProperty(value = "The workload rate of bug type task, Ignoring cancel status tasks")
  protected double bugWorkloadRate = 0d;
  @ApiModelProperty(value = "Total number of missing test bug")
  protected long missingBugNum;
  @ApiModelProperty(value = "The rate of missing test bug")
  protected double missingBugRate = 0d;
  @ApiModelProperty(value = "The number of bug that the passed")
  protected long totalPassedBugNum;
  @ApiModelProperty(value = "The number of bug that the passed at one time")
  protected long oneTimePassedBugNum;
  @ApiModelProperty(value = "One-time pass bug rate")
  protected double oneTimePassedBugRate = 0d;
  @ApiModelProperty(value = "The number of bugs by different levels")
  protected LinkedHashMap<BugLevel, Integer> bugLevelCount = new LinkedHashMap<>();
  @ApiModelProperty(value = "The rate of bugs by different levels")
  protected LinkedHashMap<BugLevel, Double> bugLevelRate = new LinkedHashMap<>();

  public double calcBugRate() {
    return calcRate(validBugNum, totalNum);
  }

  public double calcValidBugRate() {
    return calcRate(validBugNum, bugNum);
  }

  public double calcInvalidBugRate() {
    return calcRate(invalidBugNum, bugNum);
  }

  public double calcBugWorkloadRate() {
    return calcRate(bugWorkload, totalWorkload);
  }

  public double calcMissingBugRate() {
    return calcRate(missingBugNum, validBugNum);
  }

  public double calcOneTimePassedBugRate() {
    return calcRate(oneTimePassedBugNum, totalPassedBugNum);
  }

}
