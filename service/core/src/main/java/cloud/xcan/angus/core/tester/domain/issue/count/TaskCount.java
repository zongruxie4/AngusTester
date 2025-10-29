package cloud.xcan.angus.core.tester.domain.issue.count;

import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class TaskCount {

  @Schema(description = "Total number of tasks")
  private long totalTaskNum;
  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  private long validTaskNum;

  @Schema(description = "The number of tasks in pending")
  private long pendingNum;
  @Schema(description = "The number of tasks in progress")
  private long inProgressNum;
  @Schema(description = "The number of tasks is confirming")
  private long confirmingNum;
  @Schema(description = "The number of completed tasks")
  private long completedNum;
  @Schema(description = "The number of canceled tasks")
  private long canceledNum;
  @Schema(description = "Total number of tasks")
  private long totalStatusNum;

  @Schema(description = "The number of Performance Tasks")
  private long perfNum;
  @Schema(description = "The number of function tasks")
  private long functionalNum;
  @Schema(description = "The number of stabilization tasks")
  private long stabilityNum;
  @Schema(description = "Total number of test type tasks")
  private long totalTestTypeNum;

  @Schema(description = "The number of requirement type tasks")
  private long requirementNum;
  @Schema(description = "The number of story type tasks")
  private long storyNum;
  @Schema(description = "The number of task type tasks")
  private long taskNum;
  @Schema(description = "The number of bug type tasks")
  private long bugNum;
  @Schema(description = "The number of api test type tasks")
  private long apiTestNum;
  @Schema(description = "The number of scenario test type tasks")
  private long scenarioTestNum;
  @Schema(description = "Total number of task type tasks")
  private long totalTaskTypeNum;

  @Schema(description = "The number of successful test tasks")
  private long testSuccessNum;
  @Schema(description = "The number of test failed tasks")
  private long testFailNum;

  @Schema(description = "The number of overdue tasks")
  private long overdueNum;

  @Schema(description = "The number of tasks that the test passed at one time")
  private long oneTimePassedNum;
  @NumberFormat("#.##")
  @Schema(description = "One-time pass rate")
  private BigDecimal oneTimePassedRate = BigDecimal.ZERO;
  //@Schema(description = "The number of tasks that the passed not at one time")
  //private long oneTimeNotPassedNum;
  //@NumberFormat("#.##")
  //@Schema(description = "One-time not pass rate")
  //private BigDecimal oneTimeNotPassedRate = BigDecimal.ZERO;

  @Schema(description = "The number of task process times")
  private long processTimes;
  @Schema(description = "The number of task process failure times")
  private long processFailTimes;

  @NumberFormat("#.##")
  @Schema(description = "Eval workload")
  private BigDecimal evalWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @Schema(description = "Actual workload")
  private BigDecimal actualWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @Schema(description = "Completed workload")
  private BigDecimal completedWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @Schema(description = "Actual saving workload")
  private BigDecimal savingWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @Schema(description = "Rate of actual saving workload")
  private BigDecimal savingWorkloadRate = BigDecimal.ZERO;

  public double getProgress() {
    if (validTaskNum == 0) {
      return 0;
    }
    double progress = (double) completedNum / validTaskNum;
    DecimalFormat df = new DecimalFormat("0.00");
    return Double.parseDouble(df.format(progress * 100));
  }

  public double getOverdueRate() {
    if (validTaskNum == 0) {
      return 0;
    }
    double progress = (double) overdueNum / validTaskNum;
    DecimalFormat df = new DecimalFormat("0.00");
    return Double.parseDouble(df.format(progress * 100));
  }
}
