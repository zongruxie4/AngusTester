package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TesterSubmittedBugCount implements BugCountBase, TestCaseHitCount {

  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  private long totalNum;
  @ApiModelProperty(value = "Total evaluate workload, Ignoring cancel status tasks")
  private double totalWorkload = 0d;

  // Submitted Bug by Creator(The vast majority of cases are submitted by testers)
  @ApiModelProperty(value = "Total number of bug type task")
  private long bugNum;
  @ApiModelProperty(value = "Total rate of bug type task")
  private double bugRate = 0d;
  @ApiModelProperty(value = "Total number of valid bug type task, Ignoring cancel status tasks")
  private long validBugNum;
  @ApiModelProperty(value = "The rate of valid bug type task, Ignoring cancel status tasks")
  private double validBugRate = 0d;
  @ApiModelProperty(value = "Total number of invalid bug type task")
  private long invalidBugNum;
  @ApiModelProperty(value = "The rate of invalid bug type task")
  private double invalidBugRate = 0d;
  @ApiModelProperty(value = "Total workload of bug type task, Ignoring cancel status tasks")
  private double bugWorkload;
  @ApiModelProperty(value = "The workload rate of bug type task, Ignoring cancel status tasks")
  private double bugWorkloadRate = 0d;
  @ApiModelProperty(value = "Total number of missing test bug")
  private long missingBugNum;
  @ApiModelProperty(value = "The rate of missing test bug")
  private double missingBugRate = 0d;
  @ApiModelProperty(value = "The number of bug that the passed")
  protected long totalPassedBugNum;
  @ApiModelProperty(value = "The number of bug that the passed at one time")
  private long oneTimePassedBugNum;
  @ApiModelProperty(value = "One-time pass bug rate")
  private double oneTimePassedBugRate = 0d;
  @ApiModelProperty(value = "The number of bugs by different levels")
  private LinkedHashMap<BugLevel, Integer> bugLevelCount = new LinkedHashMap<>();
  @ApiModelProperty(value = "The rate of bugs by different levels")
  private LinkedHashMap<BugLevel, Double> bugLevelRate = new LinkedHashMap<>();

  // Test hits
  @ApiModelProperty(value = "The total num of cases testing hit")
  private long testCaseHitNum;
  @ApiModelProperty(value = "The rate of cases testing hit ")
  private double testCaseHitRate;

  @Override
  public double calcBugRate() {
    return calcRate(bugNum, totalNum);
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

  public double calcTestCaseHitRate() {
    return calcRate(testCaseHitNum, totalNum);
  }
}
