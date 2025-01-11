package cloud.xcan.sdf.core.angustester.domain.task.count;

import static java.lang.String.valueOf;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TesterSubmittedBugDetail implements DataDetailBase {

  private String name;

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
  @ApiModelProperty(value = "The total num of cases testing hit")
  private long testCaseHitNum;
  @ApiModelProperty(value = "The rate of cases testing hit ")
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
