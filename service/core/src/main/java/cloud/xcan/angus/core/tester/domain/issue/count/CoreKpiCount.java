package cloud.xcan.angus.core.tester.domain.issue.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiCount {

  @Schema(description = "Total number of tasks, Ignoring cancel status tasks")
  protected long totalNum;
  @Schema(description = "The number of completed tasks")
  protected long completedNum;
  @Schema(description = "The rate of completed tasks")
  protected double completedRate;

  @Schema(description = "Evaluate workload")
  protected double evalWorkload = 0d;
  @Schema(description = "Completed workload")
  protected double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

  @Schema(description = "The number of overdue tasks")
  protected long overdueNum;
  @Schema(description = "The rate of overdue tasks")
  protected double overdueRate = 0d;
  @Schema(description = "The number of completed overdue tasks")
  protected long completedOverdueNum;
  @Schema(description = "The rate of completed overdue tasks")
  protected double completedOverdueRate = 0d;

  @Schema(description = "The number of bug type tasks")
  protected long bugNum;
  @Schema(description = "The rate of bug type tasks")
  protected double bugRate = 0d;
  @Schema(description = "The number of completed bug type tasks")
  protected long completedBugNum;
  @Schema(description = "The rate of completed bug type tasks")
  protected double completedBugRate = 0d;

}
