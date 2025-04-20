package cloud.xcan.angus.core.tester.domain.func.cases.count;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiCount {

  @Schema(description = "Total number of cases, Ignoring cancel status cases")
  protected long totalNum;
  @Schema(description = "The number of completed cases")
  protected long completedNum;
  @Schema(description = "The rate of completed cases")
  protected double completedRate;

  @Schema(description = "Evaluate workload")
  protected double evalWorkload = 0d;
  @Schema(description = "Completed workload")
  protected double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

  @Schema(description = "The number of overdue cases")
  protected long overdueNum;
  @Schema(description = "The rate of overdue cases")
  protected double overdueRate = 0d;
  @Schema(description = "The number of completed overdue cases")
  protected long completedOverdueNum;
  @Schema(description = "The rate of completed overdue cases")
  protected double completedOverdueRate = 0d;

  @Schema(description = "The number of review cases")
  protected long reviewNum;
  @Schema(description = "The rate of review cases")
  protected double reviewRate = 0d;
  @Schema(description = "The number of completed review cases")
  protected long completedReviewNum;
  @Schema(description = "The rate of completed review cases")
  protected double completedReviewRate = 0d;

}
