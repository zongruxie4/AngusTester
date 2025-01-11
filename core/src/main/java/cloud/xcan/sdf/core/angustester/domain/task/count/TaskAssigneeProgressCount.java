package cloud.xcan.sdf.core.angustester.domain.task.count;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
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
public class TaskAssigneeProgressCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long assigneeId;
  private String assigneeName;
  @ExcelIgnore
  private String assigneeAvatar;

  @ApiModelProperty(value = "The total number of tasks")
  private int totalTaskNum;
  @ApiModelProperty(value = "The total number of tasks, Ignoring cancel status task")
  private int validTaskNum;

  @ApiModelProperty(value = "Evaluate workload")
  private double evalWorkload;
  @ApiModelProperty(value = "Actual workload")
  private double actualWorkload;
  @ApiModelProperty(value = "Completed workload")
  private double completedWorkload;
  @ApiModelProperty(value = "The rate of completed workload")
  private double completedWorkloadRate;

  @ApiModelProperty(value = "The number of completed task")
  private long completedNum;
  @ApiModelProperty(value = "The rate of passed task")
  private double completedRate;

  ///////////////Overdue///////////////
  @ApiModelProperty(value = "The number of overdue task")
  private long overdueNum;
  //@NumberFormat("#.##")
  @ApiModelProperty(value = "The rate of task overdue")
  private double overdueRate;

  public double getProgress() {
    return completedRate;
  }
}
