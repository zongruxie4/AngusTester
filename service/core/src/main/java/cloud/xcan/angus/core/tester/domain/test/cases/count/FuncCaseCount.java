package cloud.xcan.angus.core.tester.domain.test.cases.count;

import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseCount {

  //@Schema(description = "The total number of plans than used by cases")
  //private int totalUsedPlan;
  //@Schema(description = "The total number of plans")
  //private int totalPlan;
  @Schema(description = "The total number of cases")
  private int totalCaseNum;
  @Schema(description = "The total number of tested cases, Ignoring cancel status cases")
  private int validCaseNum;
  @Schema(description = "The total number of already tested cases")
  private int totalTestedCaseNum;

  @Schema(description = "The number of overdue cases")
  private int overdueNum;

  @Schema(description = "The number of pending review cases")
  private int pendingReviewNum;
  @Schema(description = "The number of passed review cases")
  private int passedReviewNum;
  @Schema(description = "The number of failed review cases")
  private int failedReviewNum;
  @Schema(description = "The total number of review cases")
  private int totalReviewCaseNum;
  @Schema(description = "The total number of already reviewed cases")
  private int totalReviewedCaseNum;
  @Schema(description = "The total number(times) of review")
  private int totalReviewTimes;

  @Schema(description = "The number of UI test cases")
  private int uiTestNum;
  @Schema(description = "The number of api test cases")
  private int apiTestNum;
  @Schema(description = "The number of unit test cases")
  private int unitTestNum;
  @Schema(description = "The total number of integration test cases")
  private int integrationTestNum;
  @Schema(description = "The total number of e2e test cases")
  private int e2eTestNum;

  @Schema(description = "The number of cases that the review passed at one time")
  private int oneTimePassReviewNum;
  @NumberFormat("#.##")
  @Schema(description = "One-time pass review rate")
  private BigDecimal oneTimePassReviewRate = BigDecimal.ZERO;

  @Schema(description = "The number of pending test cases")
  private int pendingTestNum;
  @Schema(description = "The number of passed test cases")
  private int passedTestNum;
  @Schema(description = "The number of not passed test cases")
  private int notPassedTestNum;
  @Schema(description = "The number of blocked test cases")
  private int blockedTestNum;
  @Schema(description = "The number of canceled test cases")
  private int canceledTestNum;

  @Schema(description = "The number of cases that the test passed at one time")
  private int oneTimePassedTestNum;
  @NumberFormat("#.##")
  @Schema(description = "One-time pass test rate")
  private BigDecimal oneTimePassedTestRate = BigDecimal.ZERO;

  @Schema(description = "The total times of test")
  private int totalTestTimes;
  @Schema(description = "The total times of test failure")
  private int totalTestFailTimes;

  @NumberFormat("#.##")
  @Schema(description = "Eval workload")
  private BigDecimal evalWorkload = BigDecimal.ZERO;
  @Schema(description = "Actual workload")
  @NumberFormat("#.##")
  private BigDecimal actualWorkload = BigDecimal.ZERO;
  @Schema(description = "Completed workload")
  @NumberFormat("#.##")
  private BigDecimal completedWorkload = BigDecimal.ZERO;

  public double getProgress() {
    if (validCaseNum == 0) {
      return 0;
    }
    double progress = (double) passedTestNum / validCaseNum;
    DecimalFormat df = new DecimalFormat("0.00");
    return Double.parseDouble(df.format(progress * 100));
  }

  public double getOverdueRate() {
    if (validCaseNum == 0) {
      return 0;
    }
    double progress = (double) overdueNum / validCaseNum;
    DecimalFormat df = new DecimalFormat("0.00");
    return Double.parseDouble(df.format(progress * 100));
  }
}
