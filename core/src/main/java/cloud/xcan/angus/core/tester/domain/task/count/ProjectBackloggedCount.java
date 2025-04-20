package cloud.xcan.angus.core.tester.domain.task.count;

import cloud.xcan.angus.spec.annotations.Unused;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Total number of tasks")
  protected long totalNum;
  @Schema(description = "Total workload of tasks")
  protected double totalWorkload;
  @Schema(description = "The completion time for all processed tasks")
  protected double processedInDay;
  @Schema(description = "Daily processed average workload of sprint tasks")
  protected double dailyProcessedWorkload;
  @Schema(description = "Daily processed average number of sprint tasks")
  protected double dailyProcessedNum;*/

  // Backlog :: @Unused
  @Schema(description = "The number of backlog")
  private long backlogNum;
  @Schema(description = "The rate of backlog to total tasks")
  private double backlogRate;
  @Schema(description = "Evaluate the completion time for backlog workload")
  private double backlogWorkloadCompletionTime;
  @Schema(description = "Evaluate the completion time for backlog number")
  private double backlogNumCompletionTime;

  // Unfinished sprint tasks ::  @Unused
  @Schema(description = "The number of unfinished tasks in the sprint")
  private long sprintBackloggedNum;
  @Schema(description = "The rate of unfinished tasks in the sprint")
  private double sprintBackloggedRate;
  @Schema(description = "The workload of unfinished tasks in the sprint")
  protected double sprintBackloggedTaskWorkload;
  @Schema(description = "The workload rate of unfinished tasks in the sprint")
  protected double sprintBackloggedWorkloadRate;
  @Schema(description = "Evaluate the completion time for unfinished tasks in sprint")
  protected double sprintBackloggedCompletionTime;

  /*// Total Backlogged (Backlog + Unfinished sprint tasks)
  @Schema(description = "The number of unfinished tasks in the sprint or project")
  protected long backloggedNum;
  @Schema(description = "The rate of unfinished tasks in the sprint or project")
  protected double backloggedRate;
  @Schema(description = "The workload of unfinished tasks in the sprint or project")
  protected double backloggedTaskWorkload;
  @Schema(description = "The workload rate of unfinished tasks in the sprint or project")
  protected double backloggedWorkloadRate;
  @Schema(description = "The completion time for unfinished tasks evaluation in sprint or project")
  protected double backloggedCompletionTime;*/

}
