package cloud.xcan.sdf.core.angustester.domain.task.count;

import cloud.xcan.sdf.spec.annotations.Unused;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Count the backlog and unfinished sprint tasks under the project.
 * <p>
 * Note: Tasks than do not include cancellation status.
 */
@Unused
@Setter
@Getter
public class ProjectBackloggedCount extends BackloggedCount {

  /*// Total tasks
  @ApiModelProperty(value = "Total number of tasks")
  protected long totalNum;
  @ApiModelProperty(value = "Total workload of tasks")
  protected double totalWorkload;
  @ApiModelProperty(value = "The completion time for all processed tasks")
  protected double processedInDay;
  @ApiModelProperty(value = "Daily processed average workload of sprint tasks")
  protected double dailyProcessedWorkload;
  @ApiModelProperty(value = "Daily processed average number of sprint tasks")
  protected double dailyProcessedNum;*/

  // Backlog :: @Unused
  @ApiModelProperty(value = "The number of backlog")
  private long backlogNum;
  @ApiModelProperty(value = "The rate of backlog to total tasks")
  private double backlogRate;
  @ApiModelProperty(value = "Evaluate the completion time for backlog workload")
  private double backlogWorkloadCompletionTime;
  @ApiModelProperty(value = "Evaluate the completion time for backlog number")
  private double backlogNumCompletionTime;

  // Unfinished sprint tasks ::  @Unused
  @ApiModelProperty(value = "The number of unfinished tasks in the sprint")
  private long sprintBackloggedNum;
  @ApiModelProperty(value = "The rate of unfinished tasks in the sprint")
  private double sprintBackloggedRate;
  @ApiModelProperty(value = "The workload of unfinished tasks in the sprint")
  protected double sprintBackloggedTaskWorkload;
  @ApiModelProperty(value = "The workload rate of unfinished tasks in the sprint")
  protected double sprintBackloggedWorkloadRate;
  @ApiModelProperty(value = "Evaluate the completion time for unfinished tasks in sprint")
  protected double sprintBackloggedCompletionTime;

  /*// Total Backlogged (Backlog + Unfinished sprint tasks)
  @ApiModelProperty(value = "The number of unfinished tasks in the sprint or project")
  protected long backloggedNum;
  @ApiModelProperty(value = "The rate of unfinished tasks in the sprint or project")
  protected double backloggedRate;
  @ApiModelProperty(value = "The workload of unfinished tasks in the sprint or project")
  protected double backloggedTaskWorkload;
  @ApiModelProperty(value = "The workload rate of unfinished tasks in the sprint or project")
  protected double backloggedWorkloadRate;
  @ApiModelProperty(value = "The completion time for unfinished tasks evaluation in sprint or project")
  protected double backloggedCompletionTime;*/

}
