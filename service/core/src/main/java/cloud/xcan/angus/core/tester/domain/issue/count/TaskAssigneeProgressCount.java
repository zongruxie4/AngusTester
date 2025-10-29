package cloud.xcan.angus.core.tester.domain.issue.count;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
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
public class TaskAssigneeProgressCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long assigneeId;
  private String assigneeName;
  @ExcelIgnore
  private String assigneeAvatar;

  @Schema(description = "The total number of tasks")
  private int totalTaskNum;
  @Schema(description = "The total number of tasks, Ignoring cancel status task")
  private int validTaskNum;

  @Schema(description = "Evaluate workload")
  private double evalWorkload;
  @Schema(description = "Actual workload")
  private double actualWorkload;
  @Schema(description = "Completed workload")
  private double completedWorkload;
  @Schema(description = "The rate of completed workload")
  private double completedWorkloadRate;

  @Schema(description = "The number of completed task")
  private long completedNum;
  @Schema(description = "The rate of passed task")
  private double completedRate;

  ///////////////Overdue///////////////
  @Schema(description = "The number of overdue task")
  private long overdueNum;
  //@NumberFormat("#.##")
  @Schema(description = "The rate of task overdue")
  private double overdueRate;

  public double getProgress() {
    return completedRate;
  }
}
