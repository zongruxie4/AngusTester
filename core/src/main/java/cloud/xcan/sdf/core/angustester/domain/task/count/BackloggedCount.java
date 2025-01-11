package cloud.xcan.sdf.core.angustester.domain.task.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Count the unfinished tasks under the sprint.
 * <p>
 * Note: Tasks than do not include cancellation status.
 */
@Setter
@Getter
public class BackloggedCount {

  // Total tasks
  @ApiModelProperty(value = "Total number of tasks or cases")
  protected long totalNum;
  @ApiModelProperty(value = "Total workload of tasks or cases")
  protected double totalWorkload;
  @ApiModelProperty(value = "The completion time for all processed tasks or cases")
  protected long processedInDay;
  @ApiModelProperty(value = "Daily processed average number of tasks or cases")
  protected double dailyProcessedNum;
  @ApiModelProperty(value = "Daily processed average workload of tasks or cases")
  protected double dailyProcessedWorkload;

  // Total Backlogged (Backlog + Unfinished sprint tasks)
  @ApiModelProperty(value = "The number of unfinished tasks or cases in the plan or project")
  protected long backloggedNum;
  @ApiModelProperty(value = "The rate of unfinished tasks  or cases in the plan or project")
  protected double backloggedRate;
  @ApiModelProperty(value = "The workload of unfinished tasks or cases in the plan or project")
  protected double backloggedWorkload;
  @ApiModelProperty(value = "The workload rate of unfinished tasks or cases in the plan or project")
  protected double backloggedWorkloadRate;
  @ApiModelProperty(value = "The completion time for unfinished tasks or cases evaluation in plan or project")
  protected double backloggedCompletionTime;

}
