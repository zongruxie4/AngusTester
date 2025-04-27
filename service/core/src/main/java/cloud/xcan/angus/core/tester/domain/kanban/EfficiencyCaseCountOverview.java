package cloud.xcan.angus.core.tester.domain.kanban;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.func.cases.count.ReviewEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.func.cases.count.TestingEfficiencyCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.TestCaseHitCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.WorkloadCountBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EfficiencyCaseCountOverview implements WorkloadCountBase, UnplannedWorkCountBase,
    TestingEfficiencyCountBase, ReviewEfficiencyCountBase, LeadTimeCountBase, TestCaseHitCount {

  // Total
  @Schema(description = "Total number of cases")
  private long totalCaseNum;
  @Schema(description = "Total number of cases, Ignoring cancel status cases")
  private long validCaseNum;

  // Status
  @Schema(description = "The number of pending test cases")
  private long pendingTestNum;
  @Schema(description = "The number of passed test cases")
  private long passedTestNum;
  @Schema(description = "The number of not passed test cases")
  private long notPassedTestNum;
  @Schema(description = "The number of blocked test cases")
  private long blockedTestNum;
  @Schema(description = "The number of canceled test cases")
  private long canceledTestNum;

  // Overdue
  @Schema(description = "The number of overdue cases")
  private long overdueNum;
  @Schema(description = "Overdue rate")
  private double overdueRate = 0d;

  // Others
  @Schema(description = "Test completion progress, including test passed and not passed status")
  private double progress = 0d;
  @Schema(description = "The rate number of valid cases, Ignoring cancel status cases")
  private double validCaseRate = 0d;
  @Schema(description = "The number of invalid cases, equal to the number of cancelled status cases")
  private long invalidCaseNum;
  @Schema(description = "The rate of invalid cases, equal to the rate of cancelled status cases")
  private double invalidCaseRate = 0d;

  // Testing efficiency
  //@Schema(description = "Completed number")
  //private long passedTestNum; // Avoiding duplicate definitions
  @Schema(description = "The rate of completed work, equal to the progress")
  private double passedTestRate;
  @Schema(description = "The number of cases that the test passed at one time")
  private long oneTimePassedNum;
  @Schema(description = "One-time pass rate")
  private double oneTimePassedRate = 0d;
  @Schema(description = "The number of cases that the test passed at two time")
  private long twoTimePassedNum;
  @Schema(description = "two-time pass rate")
  private double twoTimePassedRate = 0d;
  @Schema(description = "The number of cases that the test passed not at one time")
  private long oneTimeNotPassedNum;
  @Schema(description = "One-time not pass rate")
  private double oneTimeNotPassedRate = 0d;

  // Review
  @Schema(description = "The number of pending review cases")
  private long pendingReviewNum;
  @Schema(description = "The number of passed review cases")
  private long passedReviewNum;
  @Schema(description = "The number of failed review cases")
  private long failedReviewNum;
  @Schema(description = "The total number of review cases")
  private long totalReviewCaseNum;
  @Schema(description = "The total number of not review cases")
  private long totalNotReviewCaseNum;
  @Schema(description = "The total number of already reviewed cases")
  private long totalReviewedCaseNum;

  // Review efficiency
  @Schema(description = "Review completion progress, including review passed and failed status")
  private double reviewProgress = 0d;
  @Schema(description = "The total number(times) of review")
  private int totalReviewTimes;
  @Schema(description = "The total number(times) of review failure")
  private int totalReviewFailTimes;

  //@Schema(description = "Completed review number")
  //private long passedReviewNum; // Avoiding duplicate definitions
  @Schema(description = "The rate of review work, equal to the reviewProgress")
  private double passedReviewRate;
  @Schema(description = "The number of cases that the review passed at one time")
  private long oneTimePassedReviewNum;
  @Schema(description = "One-time pass review rate")
  private double oneTimePassedReviewRate = 0d;
  @Schema(description = "The number of cases that the review passed at two time")
  private long twoTimePassedReviewNum;
  @Schema(description = "Two-time pass review rate")
  private double twoTimePassedReviewRate = 0d;
  @Schema(description = "The number of cases that the review passed not at one time")
  private long oneTimeNotPassedReviewNum;
  @Schema(description = "One-time not pass review rate")
  private double oneTimeNotPassedReviewRate = 0d;

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
  @Schema(description = "Daily processed average workload of cases")
  protected double dailyProcessedWorkload;

  /*// Submitted bug
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
  @Schema(description = "The number of case assignee")
  private long userNum;
  @Schema(description = "Tester average processing time")
  private double userAvgProcessingTime = 0d;
  @Schema(description = "Case average processing time")
  private double totalProcessingTime = 0d;
  @Schema(description = "Case average processing time")
  private double avgProcessingTime = 0d;
  @Schema(description = "Case minimum processing time")
  private double minProcessingTime = 0d;
  @Schema(description = "Case maximum processing time")
  private double maxProcessingTime = 0d;
  @Schema(description = "50th percentile case processing time")
  private double p50ProcessingTime = 0d;
  @Schema(description = "75h percentile case processing time")
  private double p75ProcessingTime = 0d;
  @Schema(description = "90th percentile case processing time")
  private double p90ProcessingTime = 0d;
  @Schema(description = "95th percentile case processing time")
  private double p95ProcessingTime = 0d;
  @Schema(description = "99th percentile case processing time")
  private double p99ProcessingTime = 0d;

  // Unplanned
  @Schema(description = "The number of unplanned cases")
  private long unplannedNum;
  @Schema(description = "The rate of unplanned cases")
  private double unplannedRate = 0d;
  @Schema(description = "The number of unplanned and completed cases")
  private long unplannedCompletedNum;
  @Schema(description = "The rate of unplanned and completed cases")
  private double unplannedCompletedRate = 0d;
  @Schema(description = "The number of unplanned task workload")
  private double unplannedWorkload = 0d;
  @Schema(description = "The rate of unplanned task workload")
  private double unplannedWorkloadRate = 0d;
  @Schema(description = "The number of unplanned and completed task workload")
  private double unplannedWorkloadCompleted = 0d;
  @Schema(description = "The rate of unplanned and completed task workload")
  private double unplannedWorkloadCompletedRate = 0d;
  /*@Schema(description = "Daily processed average workload of completed cases")
  protected double dailyProcessedWorkload;*/
  @Schema(description="Unplanned workload completion time")
  private double unplannedWorkloadProcessingTime;

  // Test hits
  @Schema(description = "The total num of cases testing hit")
  private long testCaseHitNum;
  @Schema(description = "The rate of cases testing hit ")
  private double testCaseHitRate;

  public double getActualWorkload() {
    return actualWorkload > 0 ? actualWorkload : evalWorkload;
  }

  public double calcProgress() {
    return calcRate(passedTestNum, validCaseNum);
  }

  public double calcOverdueRate() {
    return calcRate(overdueNum, validCaseNum);
  }

  public double calcValidCaseRate() {
    return calcRate(validCaseNum, totalCaseNum);
  }

  public double calcInvalidCaseRate() {
    return calcRate(invalidCaseNum, totalCaseNum);
  }

  public double calcPassedTestRate() {
    return calcRate(passedTestNum, validCaseNum);
  }

  @Override
  public double calcPassedReviewRate() {
    return calcRate(passedReviewNum, totalReviewCaseNum);
  }

  public double calcOneTimePassedRate() {
    return calcRate(oneTimePassedNum, passedTestNum);
  }

  public double calcTwoTimePassedRate() {
    return calcRate(twoTimePassedNum, passedTestNum);
  }

  public double calcOneTimeNotPassedRate() {
    return calcRate(oneTimeNotPassedNum, passedTestNum);
  }

  public double calcReviewProgress() {
    return calcRate(passedReviewNum + failedReviewNum, totalReviewCaseNum);
  }

  public double calcOneTimePassedReviewRate() {
    return calcRate(oneTimePassedReviewNum, passedReviewNum);
  }

  public double calcTwoTimePassedReviewRate() {
    return calcRate(twoTimePassedReviewNum, passedReviewNum);
  }

  public double calcOneTimeNotPassedReviewRate() {
    return calcRate(oneTimeNotPassedReviewNum, passedReviewNum);
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

  public double calcUnplannedRate() {
    return calcRate(unplannedNum, validCaseNum);
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

  public double calcTestCaseHitRate() {
    return calcRate(testCaseHitNum, validCaseNum);
  }
}
