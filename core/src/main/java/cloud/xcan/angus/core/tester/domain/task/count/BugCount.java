package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BugCount implements BugCountBase{

  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @Schema(description = "Total evaluate workload, Ignoring cancel status tasks")
  protected double totalWorkload = 0d;

  // Submitted Bug by Creator(The vast majority of cases are submitted by testers)
  @Schema(description = "Total number of bug type task")
  protected long bugNum;
  @Schema(description = "Total rate of bug type task")
  protected double bugRate = 0d;
  @Schema(description = "Total number of valid bug type task, Ignoring cancel status tasks")
  protected long validBugNum;
  @Schema(description = "The rate of valid bug type task, Ignoring cancel status tasks")
  protected double validBugRate = 0d;
  @Schema(description = "Total number of invalid bug type task")
  protected long invalidBugNum;
  @Schema(description = "The rate of invalid bug type task")
  protected double invalidBugRate = 0d;
  @Schema(description = "Total workload of bug type task, Ignoring cancel status tasks")
  protected double bugWorkload;
  @Schema(description = "The workload rate of bug type task, Ignoring cancel status tasks")
  protected double bugWorkloadRate = 0d;
  @Schema(description = "Total number of missing test bug")
  protected long missingBugNum;
  @Schema(description = "The rate of missing test bug")
  protected double missingBugRate = 0d;
  @Schema(description = "The number of bug that the passed")
  protected long totalPassedBugNum;
  @Schema(description = "The number of bug that the passed at one time")
  protected long oneTimePassedBugNum;
  @Schema(description = "One-time pass bug rate")
  protected double oneTimePassedBugRate = 0d;
  @Schema(description = "The number of bugs by different levels")
  protected LinkedHashMap<BugLevel, Integer> bugLevelCount = new LinkedHashMap<>();
  @Schema(description = "The rate of bugs by different levels")
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
