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
public class FuncTesterProgressCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long testerId;
  private String testerName;
  @ExcelIgnore
  private String testerAvatar;

  @ApiModelProperty(value = "The total number of cases")
  private int totalCaseNum;
  @ApiModelProperty(value = "The total number of cases, Ignoring cancel status cases")
  private int validCaseNum;
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

  @ApiModelProperty(value = "The number of passed test cases")
  private long passedTestNum;
  //@NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of passed test cases")
  private double passedTestRate;

  @ApiModelProperty(value = "The number of overdue cases")
  private long overdueNum;
  //@NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of test overdue")
  private double overdueRate;

  public double getActualWorkload() {
    return actualWorkload > 0 ? actualWorkload : evalWorkload;
  }

  public double getProgress() {
    return passedTestRate;
  }
}
