package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import io.swagger.annotations.ApiModelProperty;
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
public class FuncTesterCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long testerId;
  private String testerName;
  @ExcelIgnore
  private String testerAvatar;

  @ApiModelProperty(value = "The total number of cases")
  private long totalCaseNum;
  @ApiModelProperty(value = "The total number of cases, Ignoring cancel status cases")
  private long validCaseNum;

  @ApiModelProperty(value = "Evaluate workload")
  private double evalWorkload;
  @ApiModelProperty(value = "Actual workload")
  private double actualWorkload;
  @ApiModelProperty(value = "Completed workload")
  private double completedWorkload;
  @ApiModelProperty(value = "The rate of completed workload")
  private double completedWorkloadRate;
  @ApiModelProperty(value = "Actual saving workload")
  private double savingWorkload;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate of actual saving workload")
  private double savingWorkloadRate;

  ///////////////Review///////////////
  @ApiModelProperty(value = "The total number of enabled review cases, Ignoring cancel status cases")
  private long reviewNum;
  @ApiModelProperty(value = "The number of passed review")
  private long passedReviewNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of passed review")
  private double passedReviewRate;

  @ApiModelProperty(value = "The total times of review")
  private int reviewTimes;
  @ApiModelProperty(value = "The total times of review failure")
  private int reviewFailTimes;
  @ApiModelProperty(value = "The number of cases that the review passed at one time")
  private long oneTimePassedReviewNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate that the review passed at one time")
  private double oneTimePassedReviewRate;

  ///////////////Test///////////////
  @ApiModelProperty(value = "The number of passed test cases")
  private long passedTestNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of passed test cases")
  private double passedTestRate;
  @ApiModelProperty(value = "The number of failed test cases")
  private long failedTestNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of test failures")
  private double failedTestRate;

  @ApiModelProperty(value = "The total times of test")
  private int testTimes;
  @ApiModelProperty(value = "The total times of test failure")
  private int testFailTimes;
  @ApiModelProperty(value = "The number of cases that the test passed at one time")
  private long oneTimePassedTestNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate that the test passed at one time")
  private double oneTimePassedTestRate;

  ///////////////Overdue///////////////
  @ApiModelProperty(value = "The number of overdue cases")
  private long overdueNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of test overdue")
  private double overdueRate;

  public double getProgress() {
    return passedTestRate;
  }
}
