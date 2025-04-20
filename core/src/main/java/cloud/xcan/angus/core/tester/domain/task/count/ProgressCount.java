package cloud.xcan.angus.core.tester.domain.task.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ProgressCount {

  // Total
  @Schema(description = "Total number of work")
  protected long totalNum;
  @Schema(description = "The number of completed work")
  protected long completedNum;
  @Schema(description = "The rate of completed work, equal to the progress")
  protected double completedRate;

  // Workload
  @Schema(description = "Evaluate workload")
  protected double evalWorkload = 0d;
  @Schema(description = "Completed workload")
  protected double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

}
