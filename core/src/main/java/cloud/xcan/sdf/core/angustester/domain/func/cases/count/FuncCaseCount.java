package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseCount {

  //@ApiModelProperty(value = "The total number of plans than used by cases")
  //private int totalUsedPlan;
  //@ApiModelProperty(value = "The total number of plans")
  //private int totalPlan;
  @ApiModelProperty(value = "The total number of cases")
  private int totalCaseNum;
  @ApiModelProperty(value = "The total number of tested cases, Ignoring cancel status cases")
  private int validCaseNum;
  @ApiModelProperty(value = "The total number of already tested cases")
  private int totalTestedCaseNum;

  @ApiModelProperty(value = "The number of overdue cases")
  private int overdueNum;

  @ApiModelProperty(value = "The number of pending review cases")
  private int pendingReviewNum;
  @ApiModelProperty(value = "The number of passed review cases")
  private int passedReviewNum;
  @ApiModelProperty(value = "The number of failed review cases")
  private int failedReviewNum;
  @ApiModelProperty(value = "The total number of review cases")
  private int totalReviewCaseNum;
  @ApiModelProperty(value = "The total number of already reviewed cases")
  private int totalReviewedCaseNum;
  @ApiModelProperty(value = "The total number(times) of review")
  private int totalReviewTimes;

  @ApiModelProperty(value = "The number of cases that the review passed at one time")
  private int oneTimePassReviewNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "One-time pass review rate")
  private BigDecimal oneTimePassReviewRate = BigDecimal.ZERO;

  @ApiModelProperty(value = "The number of pending test cases")
  private int pendingTestNum;
  @ApiModelProperty(value = "The number of passed test cases")
  private int passedTestNum;
  @ApiModelProperty(value = "The number of not passed test cases")
  private int notPassedTestNum;
  @ApiModelProperty(value = "The number of blocked test cases")
  private int blockedTestNum;
  @ApiModelProperty(value = "The number of canceled test cases")
  private int canceledTestNum;

  @ApiModelProperty(value = "The number of cases that the test passed at one time")
  private int oneTimePassedTestNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "One-time pass test rate")
  private BigDecimal oneTimePassedTestRate = BigDecimal.ZERO;

  @ApiModelProperty(value = "The total times of test")
  private int totalTestTimes;
  @ApiModelProperty(value = "The total times of test failure")
  private int totalTestFailTimes;

  @NumberFormat("#.##")
  @ApiModelProperty(value = "Eval workload")
  private BigDecimal evalWorkload = BigDecimal.ZERO;
  @ApiModelProperty(value = "Actual workload")
  @NumberFormat("#.##")
  private BigDecimal actualWorkload = BigDecimal.ZERO;
  @ApiModelProperty(value = "Completed workload")
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
