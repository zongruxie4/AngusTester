package cloud.xcan.angus.core.tester.domain.task.count;

import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Total number of tasks or cases")
  protected long totalNum;
  @Schema(description = "Total workload of tasks or cases")
  protected double totalWorkload;
  @Schema(description = "The completion time for all processed tasks or cases")
  protected long processedInDay;
  @Schema(description = "Daily processed average number of tasks or cases")
  protected double dailyProcessedNum;
  @Schema(description = "Daily processed average workload of tasks or cases")
  protected double dailyProcessedWorkload;

  // Total Backlogged (Backlog + Unfinished sprint tasks)
  @Schema(description = "The number of unfinished tasks or cases in the plan or project")
  protected long backloggedNum;
  @Schema(description = "The rate of unfinished tasks  or cases in the plan or project")
  protected double backloggedRate;
  @Schema(description = "The workload of unfinished tasks or cases in the plan or project")
  protected double backloggedWorkload;
  @Schema(description = "The workload rate of unfinished tasks or cases in the plan or project")
  protected double backloggedWorkloadRate;
  @Schema(description = "The completion time for unfinished tasks or cases evaluation in plan or project")
  protected double backloggedCompletionTime;

}
