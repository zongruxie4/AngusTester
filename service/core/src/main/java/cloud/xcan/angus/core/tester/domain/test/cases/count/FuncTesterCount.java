package cloud.xcan.angus.core.tester.domain.test.cases.count;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.converters.longconverter.LongStringConverter;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class FuncTesterCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long testerId;
  private String testerName;
  @ExcelIgnore
  private String testerAvatar;

  @Schema(description = "The total number of cases")
  private long totalCaseNum;
  @Schema(description = "The total number of cases, Ignoring cancel status cases")
  private long validCaseNum;

  @Schema(description = "Evaluate workload")
  private double evalWorkload;
  @Schema(description = "Actual workload")
  private double actualWorkload;
  @Schema(description = "Completed workload")
  private double completedWorkload;
  @Schema(description = "The rate of completed workload")
  private double completedWorkloadRate;
  @Schema(description = "Actual saving workload")
  private double savingWorkload;
  @NumberFormat("#.##")
  @Schema(description = "Rate of actual saving workload")
  private double savingWorkloadRate;

  ///////////////Review///////////////
  @Schema(description = "The total number of enabled review cases, Ignoring cancel status cases")
  private long reviewNum;
  @Schema(description = "The number of passed review")
  private long passedReviewNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of passed review")
  private double passedReviewRate;

  @Schema(description = "The total times of review")
  private int reviewTimes;
  @Schema(description = "The total times of review failure")
  private int reviewFailTimes;
  @Schema(description = "The number of cases that the review passed at one time")
  private long oneTimePassedReviewNum;
  @NumberFormat("#.##")
  @Schema(description = "Rate that the review passed at one time")
  private double oneTimePassedReviewRate;

  ///////////////Test///////////////
  @Schema(description = "The number of passed test cases")
  private long passedTestNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of passed test cases")
  private double passedTestRate;
  @Schema(description = "The number of failed test cases")
  private long failedTestNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of test failures")
  private double failedTestRate;

  @Schema(description = "The total times of test")
  private int testTimes;
  @Schema(description = "The total times of test failure")
  private int testFailTimes;
  @Schema(description = "The number of cases that the test passed at one time")
  private long oneTimePassedTestNum;
  @NumberFormat("#.##")
  @Schema(description = "Rate that the test passed at one time")
  private double oneTimePassedTestRate;

  ///////////////Overdue///////////////
  @Schema(description = "The number of overdue cases")
  private long overdueNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of test overdue")
  private double overdueRate;

  public double getProgress() {
    return passedTestRate;
  }
}
