package cloud.xcan.sdf.core.angustester.domain.task.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiCount {

  @ApiModelProperty(value = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @ApiModelProperty(value = "The number of completed tasks")
  protected long completedNum;
  @ApiModelProperty(value = "The rate of completed tasks")
  protected double completedRate;

  @ApiModelProperty(value = "Evaluate workload")
  protected double evalWorkload = 0d;
  @ApiModelProperty(value = "Completed workload")
  protected double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

  @ApiModelProperty(value = "The number of overdue tasks")
  protected long overdueNum;
  @ApiModelProperty(value = "The rate of overdue tasks")
  protected double overdueRate = 0d;
  @ApiModelProperty(value = "The number of completed overdue tasks")
  protected long completedOverdueNum;
  @ApiModelProperty(value = "The rate of completed overdue tasks")
  protected double completedOverdueRate = 0d;

  @ApiModelProperty(value = "The number of bug type tasks")
  protected long bugNum;
  @ApiModelProperty(value = "The rate of bug type tasks")
  protected double bugRate = 0d;
  @ApiModelProperty(value = "The number of completed bug type tasks")
  protected long completedBugNum;
  @ApiModelProperty(value = "The rate of completed bug type tasks")
  protected double completedBugRate = 0d;

}
