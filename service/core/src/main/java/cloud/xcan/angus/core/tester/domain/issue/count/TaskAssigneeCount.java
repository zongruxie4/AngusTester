package cloud.xcan.angus.core.tester.domain.issue.count;

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
public class TaskAssigneeCount {

  @ExcelProperty(converter = LongStringConverter.class)
  private Long assigneeId;
  private String assigneeName;
  @ExcelIgnore
  private String assigneeAvatar;

  @Schema(description = "The total number of tasks")
  private long totalTaskNum;
  @Schema(description = "The total number of tasks, Ignoring cancel status task")
  private long validTaskNum;
  @Schema(description = "Evaluate workload")
  private double evalWorkload;
  @Schema(description = "Actual workload")
  private double actualWorkload;
  @Schema(description = "Actual saving workload")
  private double savingWorkload;
  @NumberFormat("#.##")
  @Schema(description = "Rate of actual saving workload")
  private double savingWorkloadRate;

  ///////////////Task///////////////
  @Schema(description = "The total times of processing")
  private int processTimes;
  @Schema(description = "The total times of processing failure")
  private int processFailTimes;
  @NumberFormat("#.##")
  @Schema(description = "The rate of processing failures")
  private double processFailRate;

  @Schema(description = "The number of completed task")
  private long completedNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of passed task")
  private double completedRate;

  @Schema(description = "The number of task that the passed at one time")
  private long oneTimePassedNum;
  @NumberFormat("#.##")
  @Schema(description = "Rate that the processing passed at one time")
  private double oneTimePassedRate;

  ///////////////Overdue///////////////
  @Schema(description = "The number of overdue task")
  private long overdueNum;
  @NumberFormat("#.##")
  @Schema(description = "The rate of task overdue")
  private double overdueRate;

  public double getActualWorkload() {
    return actualWorkload > 0 ? actualWorkload : evalWorkload;
  }

  public double getProgress() {
    return completedRate;
  }
}
