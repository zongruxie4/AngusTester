package cloud.xcan.sdf.core.angustester.domain.task.count;

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
public class TaskAssigneeCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long assigneeId;
  private String assigneeName;
  @ExcelIgnore
  private String assigneeAvatar;

  @ApiModelProperty(value = "The total number of tasks")
  private long totalTaskNum;
  @ApiModelProperty(value = "The total number of tasks, Ignoring cancel status task")
  private long validTaskNum;
  @ApiModelProperty(value = "Evaluate workload")
  private double evalWorkload;
  @ApiModelProperty(value = "Actual workload")
  private double actualWorkload;
  @ApiModelProperty(value = "Actual saving workload")
  private double savingWorkload;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate of actual saving workload")
  private double savingWorkloadRate;

  ///////////////Task///////////////
  @ApiModelProperty(value = "The total times of processing")
  private int processTimes;
  @ApiModelProperty(value = "The total times of processing failure")
  private int processFailTimes;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of processing failures")
  private double processFailRate;

  @ApiModelProperty(value = "The number of completed task")
  private long completedNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of passed task")
  private double completedRate;

  @ApiModelProperty(value = "The number of task that the passed at one time")
  private long oneTimePassedNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate that the processing passed at one time")
  private double oneTimePassedRate;

  ///////////////Overdue///////////////
  @ApiModelProperty(value = "The number of overdue task")
  private long overdueNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of task overdue")
  private double overdueRate;

  public double getActualWorkload() {
    return actualWorkload > 0 ? actualWorkload : evalWorkload;
  }

  public double getProgress() {
    return completedRate;
  }
}
