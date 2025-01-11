package cloud.xcan.sdf.core.angustester.domain.kanban;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.count.BugCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.ProcessingEfficiencyCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadCountBase;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EfficiencyTaskCountOverview implements WorkloadCountBase, UnplannedWorkCountBase,
    BugCountBase, ProcessingEfficiencyCountBase, FailureAssessmentCountBase, LeadTimeCountBase {

  // Total
  @ApiModelProperty(value = "Total number of tasks")
  private long totalTaskNum;
  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  private long validTaskNum;

  // Status
  @ApiModelProperty(value = "The number of tasks to be tested")
  private long pendingNum;
  @ApiModelProperty(value = "The number of tasks in progress")
  private long inProgressNum;
  @ApiModelProperty(value = "The number of tasks processed pending confirmation")
  private long confirmingNum;
  @ApiModelProperty(value = "The number of completed tasks")
  private long completedNum;
  @ApiModelProperty(value = "The number of tasks to be tested")
  private long canceledNum;

  // Overdue
  @ApiModelProperty(value = "The number of overdue tasks")
  private long overdueNum;
  @ApiModelProperty(value = "Overdue rate")
  private double overdueRate = 0d;

  // Processing efficiency
  @ApiModelProperty(value = "Task completion progress")
  private double progress = 0d;
  //@ApiModelProperty(value = "Completed number")
  //private long completedNum; // Avoiding duplicate definitions
  @ApiModelProperty(value = "The rate of completed tasks, equal to the progress")
  private double completedRate;
  @ApiModelProperty(value = "The number of tasks that the passed at one time")
  private long oneTimePassedNum;
  @ApiModelProperty(value = "One-time pass rate")
  private double oneTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of tasks that the passed at two time")
  private long twoTimePassedNum;
  @ApiModelProperty(value = "Two-time pass rate")
  private double twoTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of tasks that the passed not at one time")
  private long oneTimeNotPassedNum;
  @ApiModelProperty(value = "One-time not pass rate")
  private double oneTimeNotPassedRate = 0d;

  // Workload
  @ApiModelProperty(value = "Evaluate workload")
  private double evalWorkload = 0d;
  @ApiModelProperty(value = "Actual workload")
  private double actualWorkload = 0d;
  @ApiModelProperty(value = "Completed workload")
  private double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  private double completedWorkloadRate = 0d;
  @ApiModelProperty(value = "Actual saving workload")
  private double savingWorkload = 0d;
  @ApiModelProperty(value = "The rate of actual saving workload")
  private double savingWorkloadRate = 0d;
  @ApiModelProperty(value = "Invalid workload")
  private double invalidWorkload = 0d;
  @ApiModelProperty(value = "The rate of invalid workload")
  private double invalidWorkloadRate = 0d;
  @ApiModelProperty(value = "Daily processed average workload of tasks")
  protected double dailyProcessedWorkload;

  // Bug
  @ApiModelProperty(value = "Total number of bug type task")
  private long bugNum;
  @ApiModelProperty(value = "Total rate of bug type task")
  private double bugRate;
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

  // Failure (Bug Handling Times)
  @ApiModelProperty(value = "The total number of bugs handling times")
  private long failureNum;
  @ApiModelProperty(value = "The total workload of bugs")
  private double failureWorkload;
  @ApiModelProperty(value = "The number of bugs handling that exceed one")
  private long oneTimeFailureNum;
  @ApiModelProperty(value = "The rate of bugs handling that exceed one")
  private double oneTimeFailureRate = 0d;
  @ApiModelProperty(value = "The number of bugs handling that exceed two")
  private long twoTimeFailureNum;
  @ApiModelProperty(value = "The rate of bugs handling that exceed two")
  private double twoTimeFailureRate = 0d;
  @ApiModelProperty(value = "The number of completed failures")
  private long failureCompletedNum;
  @ApiModelProperty(value = "The rate of completed failures")
  private double failureCompletedRate;
  @ApiModelProperty(value = "The number of overdue failures")
  private long failureOverdueNum;
  @ApiModelProperty(value = "The rate of overdue failures")
  private double failureOverdueRate;
  @ApiModelProperty(value = "The total time of completed failures")
  private double failureTotalTime;
  @ApiModelProperty(value = "The min time of completed failures")
  private double failureMinTime;
  @ApiModelProperty(value = "The max time of completed failures")
  private double failureMaxTime;
  @ApiModelProperty(value = "The average time of completed failures")
  private double failureAvgTime;
  @ApiModelProperty(value = "The number of failures by different levels")
  private LinkedHashMap<BugLevel, Integer> failureLevelCount = new LinkedHashMap<>();
  @ApiModelProperty(value = "The rate of failures by different levels")
  private LinkedHashMap<BugLevel, Double> failureLevelRate = new LinkedHashMap<>();

  /* // Submitted bug
  @ApiModelProperty(value = "The number of cases that the review passed at two time")
  private long submittedBugNum;
  @ApiModelProperty(value = "The number of cases that the review passed at two time")
  private long submittedValidBugNum;
  @ApiModelProperty(value = "Rate of actual saving workload")
  private double submittedValidBugRate = 0d;
  @ApiModelProperty(value = "The number of cases that the review passed at two time")
  private long submittedInvalidBugNum;
  @ApiModelProperty(value = "Rate of actual saving workload")
  private double submittedInvalidBugRate = 0d;
  @ApiModelProperty(value = "Rate of test cases that found bugs")
  private double testHitRate = 0d;*/

  // Lead time
  @ApiModelProperty(value = "The number of task assignee")
  private long userNum;
  @ApiModelProperty(value = "Assignee average processing time")
  private double userAvgProcessingTime = 0d;
  @ApiModelProperty(value = "Task average processing time")
  private double totalProcessingTime = 0d;
  @ApiModelProperty(value = "Task average processing time")
  private double avgProcessingTime = 0d;
  @ApiModelProperty(value = "Task minimum processing time")
  private double minProcessingTime = 0d;
  @ApiModelProperty(value = "Task maximum processing time")
  private double maxProcessingTime = 0d;
  @ApiModelProperty(value = "50th percentile task processing time")
  private double p50ProcessingTime = 0d;
  @ApiModelProperty(value = "75h percentile task processing time")
  private double p75ProcessingTime = 0d;
  @ApiModelProperty(value = "90th percentile task processing time")
  private double p90ProcessingTime = 0d;
  @ApiModelProperty(value = "95th percentile task processing time")
  private double p95ProcessingTime = 0d;
  @ApiModelProperty(value = "99th percentile task processing time")
  private double p99ProcessingTime = 0d;

  // Unplanned(exclude bug)
  @ApiModelProperty(value = "The number of unplanned tasks")
  private long unplannedNum;
  @ApiModelProperty(value = "The rate of unplanned tasks")
  private double unplannedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed tasks")
  private long unplannedCompletedNum;
  @ApiModelProperty(value = "The rate of unplanned and completed tasks")
  private double unplannedCompletedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned task workload")
  private double unplannedWorkload = 0d;
  @ApiModelProperty(value = "The rate of unplanned task workload")
  private double unplannedWorkloadRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed task workload")
  private double unplannedWorkloadCompleted = 0d;
  @ApiModelProperty(value = "The rate of unplanned and completed task workload")
  private double unplannedWorkloadCompletedRate = 0d;
  /*@ApiModelProperty(value = "Daily processed average workload of completed tasks")
  protected double dailyProcessedWorkload;*/
  @ApiModelProperty("Unplanned workload completion time")
  private double unplannedWorkloadProcessingTime;

  public double getActualWorkload() {
    return actualWorkload > 0 ? actualWorkload : evalWorkload;
  }

  public double calcComplatedRate() {
    return calcRate(completedNum, validTaskNum);
  }

  public double calcOverdueRate() {
    return calcRate(overdueNum, validTaskNum);
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

  public double calcCompletedWorkloadRate() {
    return calcRate(completedWorkload, evalWorkload);
  }

  public double calcSavingWorkloadRate() {
    return calcRate(savingWorkload, evalWorkload);
  }

  public double calcInvalidWorkloadRate() {
    return calcRate(invalidWorkload, evalWorkload);
  }

  public double calcBugRate() {
    return calcRate(validBugNum, validTaskNum);
  }

  public double calcValidBugRate() {
    return calcRate(validBugNum, bugNum);
  }

  public double calcInvalidBugRate() {
    return calcRate(invalidBugNum, bugNum);
  }

  public double calcBugWorkloadRate() {
    return calcRate(bugWorkload, evalWorkload);
  }

  public double calcMissingBugRate() {
    return calcRate(missingBugNum, validBugNum);
  }

  public double calcOneTimePassedBugRate() {
    return calcRate(oneTimePassedBugNum, totalPassedBugNum);
  }

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

  public double calcUnplannedRate() {
    return calcRate(unplannedNum, validTaskNum/* - validBugNum*/);
  }

  public double calcUnplannedCompletedRate() {
    return calcRate(unplannedCompletedNum, unplannedNum);
  }

  public double calcUnplannedWorkloadRate() {
    return calcRate(unplannedWorkload, evalWorkload);
  }

  public double calcUnplannedWorkloadCompletedRate() {
    return calcRate(unplannedWorkloadCompleted, unplannedWorkload);
  }
}
