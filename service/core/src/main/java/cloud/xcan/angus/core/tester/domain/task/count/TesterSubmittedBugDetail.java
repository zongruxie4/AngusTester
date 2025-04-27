package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TesterSubmittedBugDetail implements DataDetailBase {

  private String name;

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

  /**
   * The number and rate of failures by different levels.
   */
  private long criticalNum;
  private double criticalRate;
  private long majorNum;
  private double majorRate;
  private long minorNum;
  private double minorRate;
  private long trivialNum;
  private double trivialRate;

  // Test hits
  @Schema(description = "The total num of cases testing hit")
  private long testCaseHitNum;
  @Schema(description = "The rate of cases testing hit ")
  private double testCaseHitRate;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(totalWorkload),
        valueOf(bugNum), valueOf(bugRate),
        valueOf(validBugNum), valueOf(validBugRate),
        valueOf(invalidBugNum), valueOf(invalidBugRate),
        valueOf(bugWorkload), valueOf(bugWorkloadRate),
        valueOf(missingBugNum), valueOf(missingBugRate),
        valueOf(totalPassedBugNum),
        valueOf(oneTimePassedBugNum), valueOf(oneTimePassedBugRate),
        valueOf(criticalNum), valueOf(criticalRate),
        valueOf(majorNum), valueOf(majorRate),
        valueOf(minorNum), valueOf(minorRate),
        valueOf(trivialNum), valueOf(trivialRate),
        valueOf(testCaseHitNum), valueOf(testCaseHitRate)
    };
  }
}
