package cloud.xcan.sdf.core.angustester.domain.task.count;

import com.alibaba.excel.annotation.format.NumberFormat;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class TaskCount {

  @ApiModelProperty(value = "Total number of tasks")
  private long totalTaskNum;
  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  private long validTaskNum;

  @ApiModelProperty(value = "The number of tasks in pending")
  private long pendingNum;
  @ApiModelProperty(value = "The number of tasks in progress")
  private long inProgressNum;
  @ApiModelProperty(value = "The number of tasks is confirming")
  private long confirmingNum;
  @ApiModelProperty(value = "The number of completed tasks")
  private long completedNum;
  @ApiModelProperty(value = "The number of canceled tasks")
  private long canceledNum;
  @ApiModelProperty(value = "Total number of tasks")
  private long totalStatusNum;

  @ApiModelProperty(value = "The number of Performance Tasks")
  private long perfNum;
  @ApiModelProperty(value = "The number of function tasks")
  private long functionalNum;
  @ApiModelProperty(value = "The number of stabilization tasks")
  private long stabilityNum;
  @ApiModelProperty(value = "Total number of test type tasks")
  private long totalTestTypeNum;

  @ApiModelProperty(value = "The number of requirement type tasks")
  private long requirementNum;
  @ApiModelProperty(value = "The number of story type tasks")
  private long storyNum;
  @ApiModelProperty(value = "The number of task type tasks")
  private long taskNum;
  @ApiModelProperty(value = "The number of bug type tasks")
  private long bugNum;
  @ApiModelProperty(value = "The number of api test type tasks")
  private long apiTestNum;
  @ApiModelProperty(value = "The number of scenario test type tasks")
  private long scenarioTestNum;
  @ApiModelProperty(value = "Total number of task type tasks")
  private long totalTaskTypeNum;

  @ApiModelProperty(value = "The number of successful test tasks")
  private long testSuccessNum;
  @ApiModelProperty(value = "The number of test failed tasks")
  private long testFailNum;

  @ApiModelProperty(value = "The number of overdue tasks")
  private long overdueNum;

  @ApiModelProperty(value = "The number of tasks that the test passed at one time")
  private long oneTimePassedNum;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "One-time pass rate")
  private BigDecimal oneTimePassedRate = BigDecimal.ZERO;
  //@ApiModelProperty(value = "The number of tasks that the passed not at one time")
  //private long oneTimeNotPassedNum;
  //@NumberFormat("#.##")
  //@ApiModelProperty(value = "One-time not pass rate")
  //private BigDecimal oneTimeNotPassedRate = BigDecimal.ZERO;

  @ApiModelProperty(value = "The number of task process times")
  private long processTimes;
  @ApiModelProperty(value = "The number of task process failure times")
  private long processFailTimes;

  @NumberFormat("#.##")
  @ApiModelProperty(value = "Eval workload")
  private BigDecimal evalWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Actual workload")
  private BigDecimal actualWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Completed workload")
  private BigDecimal completedWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Actual saving workload")
  private BigDecimal savingWorkload = BigDecimal.ZERO;
  @NumberFormat("#.##")
  @ApiModelProperty(value = "Rate of actual saving workload")
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
