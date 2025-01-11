package cloud.xcan.sdf.core.angustester.domain.kanban;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.func.cases.count.ReviewEfficiencyCountBase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.TestingEfficiencyCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.TestCaseHitCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.WorkloadCountBase;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EfficiencyCaseCountOverview implements WorkloadCountBase, UnplannedWorkCountBase,
    TestingEfficiencyCountBase, ReviewEfficiencyCountBase, LeadTimeCountBase, TestCaseHitCount {

  // Total
  @ApiModelProperty(value = "Total number of cases")
  private long totalCaseNum;
  @ApiModelProperty(value = "Total number of cases, Ignoring cancel status cases")
  private long validCaseNum;

  // Status
  @ApiModelProperty(value = "The number of pending test cases")
  private long pendingTestNum;
  @ApiModelProperty(value = "The number of passed test cases")
  private long passedTestNum;
  @ApiModelProperty(value = "The number of not passed test cases")
  private long notPassedTestNum;
  @ApiModelProperty(value = "The number of blocked test cases")
  private long blockedTestNum;
  @ApiModelProperty(value = "The number of canceled test cases")
  private long canceledTestNum;

  // Overdue
  @ApiModelProperty(value = "The number of overdue cases")
  private long overdueNum;
  @ApiModelProperty(value = "Overdue rate")
  private double overdueRate = 0d;

  // Others
  @ApiModelProperty(value = "Test completion progress, including test passed and not passed status")
  private double progress = 0d;
  @ApiModelProperty(value = "The rate number of valid cases, Ignoring cancel status cases")
  private double validCaseRate = 0d;
  @ApiModelProperty(value = "The number of invalid cases, equal to the number of cancelled status cases")
  private long invalidCaseNum;
  @ApiModelProperty(value = "The rate of invalid cases, equal to the rate of cancelled status cases")
  private double invalidCaseRate = 0d;

  // Testing efficiency
  //@ApiModelProperty(value = "Completed number")
  //private long passedTestNum; // Avoiding duplicate definitions
  @ApiModelProperty(value = "The rate of completed work, equal to the progress")
  private double passedTestRate;
  @ApiModelProperty(value = "The number of cases that the test passed at one time")
  private long oneTimePassedNum;
  @ApiModelProperty(value = "One-time pass rate")
  private double oneTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of cases that the test passed at two time")
  private long twoTimePassedNum;
  @ApiModelProperty(value = "two-time pass rate")
  private double twoTimePassedRate = 0d;
  @ApiModelProperty(value = "The number of cases that the test passed not at one time")
  private long oneTimeNotPassedNum;
  @ApiModelProperty(value = "One-time not pass rate")
  private double oneTimeNotPassedRate = 0d;

  // Review
  @ApiModelProperty(value = "The number of pending review cases")
  private long pendingReviewNum;
  @ApiModelProperty(value = "The number of passed review cases")
  private long passedReviewNum;
  @ApiModelProperty(value = "The number of failed review cases")
  private long failedReviewNum;
  @ApiModelProperty(value = "The total number of review cases")
  private long totalReviewCaseNum;
  @ApiModelProperty(value = "The total number of not review cases")
  private long totalNotReviewCaseNum;
  @ApiModelProperty(value = "The total number of already reviewed cases")
  private long totalReviewedCaseNum;

  // Review efficiency
  @ApiModelProperty(value = "Review completion progress, including review passed and failed status")
  private double reviewProgress = 0d;
  @ApiModelProperty(value = "The total number(times) of review")
  private int totalReviewTimes;
  @ApiModelProperty(value = "The total number(times) of review failure")
  private int totalReviewFailTimes;

  //@ApiModelProperty(value = "Completed review number")
  //private long passedReviewNum; // Avoiding duplicate definitions
  @ApiModelProperty(value = "The rate of review work, equal to the reviewProgress")
  private double passedReviewRate;
  @ApiModelProperty(value = "The number of cases that the review passed at one time")
  private long oneTimePassedReviewNum;
  @ApiModelProperty(value = "One-time pass review rate")
  private double oneTimePassedReviewRate = 0d;
  @ApiModelProperty(value = "The number of cases that the review passed at two time")
  private long twoTimePassedReviewNum;
  @ApiModelProperty(value = "Two-time pass review rate")
  private double twoTimePassedReviewRate = 0d;
  @ApiModelProperty(value = "The number of cases that the review passed not at one time")
  private long oneTimeNotPassedReviewNum;
  @ApiModelProperty(value = "One-time not pass review rate")
  private double oneTimeNotPassedReviewRate = 0d;

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
  @ApiModelProperty(value = "Daily processed average workload of cases")
  protected double dailyProcessedWorkload;

  /*// Submitted bug
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
  @ApiModelProperty(value = "The number of case assignee")
  private long userNum;
  @ApiModelProperty(value = "Tester average processing time")
  private double userAvgProcessingTime = 0d;
  @ApiModelProperty(value = "Case average processing time")
  private double totalProcessingTime = 0d;
  @ApiModelProperty(value = "Case average processing time")
  private double avgProcessingTime = 0d;
  @ApiModelProperty(value = "Case minimum processing time")
  private double minProcessingTime = 0d;
  @ApiModelProperty(value = "Case maximum processing time")
  private double maxProcessingTime = 0d;
  @ApiModelProperty(value = "50th percentile case processing time")
  private double p50ProcessingTime = 0d;
  @ApiModelProperty(value = "75h percentile case processing time")
  private double p75ProcessingTime = 0d;
  @ApiModelProperty(value = "90th percentile case processing time")
  private double p90ProcessingTime = 0d;
  @ApiModelProperty(value = "95th percentile case processing time")
  private double p95ProcessingTime = 0d;
  @ApiModelProperty(value = "99th percentile case processing time")
  private double p99ProcessingTime = 0d;

  // Unplanned
  @ApiModelProperty(value = "The number of unplanned cases")
  private long unplannedNum;
  @ApiModelProperty(value = "The rate of unplanned cases")
  private double unplannedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed cases")
  private long unplannedCompletedNum;
  @ApiModelProperty(value = "The rate of unplanned and completed cases")
  private double unplannedCompletedRate = 0d;
  @ApiModelProperty(value = "The number of unplanned task workload")
  private double unplannedWorkload = 0d;
  @ApiModelProperty(value = "The rate of unplanned task workload")
  private double unplannedWorkloadRate = 0d;
  @ApiModelProperty(value = "The number of unplanned and completed task workload")
  private double unplannedWorkloadCompleted = 0d;
  @ApiModelProperty(value = "The rate of unplanned and completed task workload")
  private double unplannedWorkloadCompletedRate = 0d;
  /*@ApiModelProperty(value = "Daily processed average workload of completed cases")
  protected double dailyProcessedWorkload;*/
  @ApiModelProperty("Unplanned workload completion time")
  private double unplannedWorkloadProcessingTime;

  // Test hits
  @ApiModelProperty(value = "The total num of cases testing hit")
  private long testCaseHitNum;
  @ApiModelProperty(value = "The rate of cases testing hit ")
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
