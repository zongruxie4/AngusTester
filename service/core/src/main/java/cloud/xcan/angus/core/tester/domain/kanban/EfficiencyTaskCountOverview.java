package cloud.xcan.angus.core.tester.domain.kanban;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.count.BugCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.ProcessingEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadCountBase;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EfficiencyTaskCountOverview implements WorkloadCountBase, UnplannedWorkCountBase,
    BugCountBase, ProcessingEfficiencyCountBase, FailureAssessmentCountBase, LeadTimeCountBase {

  // Total
  @Schema(description = "Total number of tasks")
  private long totalTaskNum;
  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  private long validTaskNum;

  // Status
  @Schema(description = "The number of tasks to be tested")
  private long pendingNum;
  @Schema(description = "The number of tasks in progress")
  private long inProgressNum;
  @Schema(description = "The number of tasks processed pending confirmation")
  private long confirmingNum;
  @Schema(description = "The number of completed tasks")
  private long completedNum;
  @Schema(description = "The number of tasks to be tested")
  private long canceledNum;

  // Overdue
  @Schema(description = "The number of overdue tasks")
  private long overdueNum;
  @Schema(description = "Overdue rate")
  private double overdueRate = 0d;

  // Processing efficiency
  @Schema(description = "Task completion progress")
  private double progress = 0d;
  //@Schema(description = "Completed number")
  //private long completedNum; // Avoiding duplicate definitions
  @Schema(description = "The rate of completed tasks, equal to the progress")
  private double completedRate;
  @Schema(description = "The number of tasks that the passed at one time")
  private long oneTimePassedNum;
  @Schema(description = "One-time pass rate")
  private double oneTimePassedRate = 0d;
  @Schema(description = "The number of tasks that the passed at two time")
  private long twoTimePassedNum;
  @Schema(description = "Two-time pass rate")
  private double twoTimePassedRate = 0d;
  @Schema(description = "The number of tasks that the passed not at one time")
  private long oneTimeNotPassedNum;
  @Schema(description = "One-time not pass rate")
  private double oneTimeNotPassedRate = 0d;

  // Workload
  @Schema(description = "Evaluate workload")
  private double evalWorkload = 0d;
  @Schema(description = "Actual workload")
  private double actualWorkload = 0d;
  @Schema(description = "Completed workload")
  private double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  private double completedWorkloadRate = 0d;
  @Schema(description = "Actual saving workload")
  private double savingWorkload = 0d;
  @Schema(description = "The rate of actual saving workload")
  private double savingWorkloadRate = 0d;
  @Schema(description = "Invalid workload")
  private double invalidWorkload = 0d;
  @Schema(description = "The rate of invalid workload")
  private double invalidWorkloadRate = 0d;
  @Schema(description = "Daily processed average workload of tasks")
  protected double dailyProcessedWorkload;

  // Bug
  @Schema(description = "Total number of bug type task")
  private long bugNum;
  @Schema(description = "Total rate of bug type task")
  private double bugRate;
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

  // Failure (Bug Handling Times)
  @Schema(description = "The total number of bugs handling times")
  private long failureNum;
  @Schema(description = "The total workload of bugs")
  private double failureWorkload;
  @Schema(description = "The number of bugs handling that exceed one")
  private long oneTimeFailureNum;
  @Schema(description = "The rate of bugs handling that exceed one")
  private double oneTimeFailureRate = 0d;
  @Schema(description = "The number of bugs handling that exceed two")
  private long twoTimeFailureNum;
  @Schema(description = "The rate of bugs handling that exceed two")
  private double twoTimeFailureRate = 0d;
  @Schema(description = "The number of completed failures")
  private long failureCompletedNum;
  @Schema(description = "The rate of completed failures")
  private double failureCompletedRate;
  @Schema(description = "The number of overdue failures")
  private long failureOverdueNum;
  @Schema(description = "The rate of overdue failures")
  private double failureOverdueRate;
  @Schema(description = "The total time of completed failures")
  private double failureTotalTime;
  @Schema(description = "The min time of completed failures")
  private double failureMinTime;
  @Schema(description = "The max time of completed failures")
  private double failureMaxTime;
  @Schema(description = "The average time of completed failures")
  private double failureAvgTime;
  @Schema(description = "The number of failures by different levels")
  private LinkedHashMap<BugLevel, Integer> failureLevelCount = new LinkedHashMap<>();
  @Schema(description = "The rate of failures by different levels")
  private LinkedHashMap<BugLevel, Double> failureLevelRate = new LinkedHashMap<>();

  /* // Submitted bug
  @Schema(description = "The number of cases that the review passed at two time")
  private long submittedBugNum;
  @Schema(description = "The number of cases that the review passed at two time")
  private long submittedValidBugNum;
  @Schema(description = "Rate of actual saving workload")
  private double submittedValidBugRate = 0d;
  @Schema(description = "The number of cases that the review passed at two time")
  private long submittedInvalidBugNum;
  @Schema(description = "Rate of actual saving workload")
  private double submittedInvalidBugRate = 0d;
  @Schema(description = "Rate of test cases that found bugs")
  private double testHitRate = 0d;*/

  // Lead time
  @Schema(description = "The number of task assignee")
  private long userNum;
  @Schema(description = "Assignee average processing time")
  private double userAvgProcessingTime = 0d;
  @Schema(description = "Task average processing time")
  private double totalProcessingTime = 0d;
  @Schema(description = "Task average processing time")
  private double avgProcessingTime = 0d;
  @Schema(description = "Task minimum processing time")
  private double minProcessingTime = 0d;
  @Schema(description = "Task maximum processing time")
  private double maxProcessingTime = 0d;
  @Schema(description = "50th percentile task processing time")
  private double p50ProcessingTime = 0d;
  @Schema(description = "75h percentile task processing time")
  private double p75ProcessingTime = 0d;
  @Schema(description = "90th percentile task processing time")
  private double p90ProcessingTime = 0d;
  @Schema(description = "95th percentile task processing time")
  private double p95ProcessingTime = 0d;
  @Schema(description = "99th percentile task processing time")
  private double p99ProcessingTime = 0d;

  // Unplanned(exclude bug)
  @Schema(description = "The number of unplanned tasks")
  private long unplannedNum;
  @Schema(description = "The rate of unplanned tasks")
  private double unplannedRate = 0d;
  @Schema(description = "The number of unplanned and completed tasks")
  private long unplannedCompletedNum;
  @Schema(description = "The rate of unplanned and completed tasks")
  private double unplannedCompletedRate = 0d;
  @Schema(description = "The number of unplanned task workload")
  private double unplannedWorkload = 0d;
  @Schema(description = "The rate of unplanned task workload")
  private double unplannedWorkloadRate = 0d;
  @Schema(description = "The number of unplanned and completed task workload")
  private double unplannedWorkloadCompleted = 0d;
  @Schema(description = "The rate of unplanned and completed task workload")
  private double unplannedWorkloadCompletedRate = 0d;
  /*@Schema(description = "Daily processed average workload of completed tasks")
  protected double dailyProcessedWorkload;*/
  @Schema(description="Unplanned workload completion time")
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
