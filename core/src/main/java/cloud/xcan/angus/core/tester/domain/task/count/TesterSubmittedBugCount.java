package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TesterSubmittedBugCount implements BugCountBase, TestCaseHitCount {

  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  private long totalNum;
  @Schema(description = "Total evaluate workload, Ignoring cancel status tasks")
  private double totalWorkload = 0d;

  // Submitted Bug by Creator(The vast majority of cases are submitted by testers)
  @Schema(description = "Total number of bug type task")
  private long bugNum;
  @Schema(description = "Total rate of bug type task")
  private double bugRate = 0d;
  @Schema(description = "Total number of valid bug type task, Ignoring cancel status tasks")
  private long validBugNum;
  @Schema(description = "The rate of valid bug type task, Ignoring cancel status tasks")
  private double validBugRate = 0d;
  @Schema(description = "Total number of invalid bug type task")
  private long invalidBugNum;
  @Schema(description = "The rate of invalid bug type task")
  private double invalidBugRate = 0d;
  @Schema(description = "Total workload of bug type task, Ignoring cancel status tasks")
  private double bugWorkload;
  @Schema(description = "The workload rate of bug type task, Ignoring cancel status tasks")
  private double bugWorkloadRate = 0d;
  @Schema(description = "Total number of missing test bug")
  private long missingBugNum;
  @Schema(description = "The rate of missing test bug")
  private double missingBugRate = 0d;
  @Schema(description = "The number of bug that the passed")
  protected long totalPassedBugNum;
  @Schema(description = "The number of bug that the passed at one time")
  private long oneTimePassedBugNum;
  @Schema(description = "One-time pass bug rate")
  private double oneTimePassedBugRate = 0d;
  @Schema(description = "The number of bugs by different levels")
  private LinkedHashMap<BugLevel, Integer> bugLevelCount = new LinkedHashMap<>();
  @Schema(description = "The rate of bugs by different levels")
  private LinkedHashMap<BugLevel, Double> bugLevelRate = new LinkedHashMap<>();

  // Test hits
  @Schema(description = "The total num of cases testing hit")
  private long testCaseHitNum;
  @Schema(description = "The rate of cases testing hit ")
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
