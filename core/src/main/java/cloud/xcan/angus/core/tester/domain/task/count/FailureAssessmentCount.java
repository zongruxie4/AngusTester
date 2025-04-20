package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FailureAssessmentCount implements FailureAssessmentCountBase {

  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @Schema(description = "Total evaluate workload, Ignoring cancel status tasks")
  protected double totalWorkload = 0d;

  @Schema(description = "Total number of bugs, Ignoring cancel status bugs")
  protected long bugNum;

  // Failure (Bug Handling Times)
  @Schema(description = "The total number of bugs handling times")
  protected long failureNum;
  @Schema(description = "The total workload of bugs")
  protected double failureWorkload;
  @Schema(description = "The number of bugs handling that exceed one")
  protected long oneTimeFailureNum;
  @Schema(description = "The rate of bugs handling that exceed one")
  protected double oneTimeFailureRate = 0d;
  @Schema(description = "The number of bugs handling that exceed two")
  protected long twoTimeFailureNum;
  @Schema(description = "The rate of bugs handling that exceed two")
  protected double twoTimeFailureRate = 0d;
  @Schema(description = "The number of completed failures")
  protected long failureCompletedNum;
  @Schema(description = "The rate of completed failures")
  protected double failureCompletedRate;
  @Schema(description = "The number of overdue failures")
  protected long failureOverdueNum;
  @Schema(description = "The rate of overdue failures")
  protected double failureOverdueRate;

  @Schema(description = "The total overdue time of completed failures")
  protected double failureTotalTime;
  @Schema(description = "The average overdue time of completed failures")
  protected double failureAvgTime;
  @Schema(description = "The min overdue time of completed failures")
  protected double failureMinTime;
  @Schema(description = "The max overdue time of completed failures")
  protected double failureMaxTime;

  @Schema(description = "The number of failures by different levels")
  protected LinkedHashMap<BugLevel, Integer> failureLevelCount = new LinkedHashMap<>();
  @Schema(description = "The rate of failures by different levels")
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
