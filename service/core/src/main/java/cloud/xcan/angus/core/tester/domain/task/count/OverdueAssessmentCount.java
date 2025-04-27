package cloud.xcan.angus.core.tester.domain.task.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.angus.core.tester.domain.kanban.OverdueRiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class OverdueAssessmentCount {

  @Schema(description="The overdue risk level of work")
  protected OverdueRiskLevel riskLevel;

  @Schema(description = "Total number of tasks or cases, Ignoring cancel status tasks or cases")
  protected long totalNum;
  @Schema(description = "Total evaluate workload, Ignoring cancel status tasks or cases")
  protected double totalWorkload = 0d;

  @Schema(description="The overdue number of work tasks or cases")
  protected long overdueNum;
  @Schema(description="The overdue rate of work tasks or cases")
  protected double overdueRate;

  @Schema(description="The overdue workload of work tasks or cases")
  protected double overdueWorkload;
  @Schema(description="The overdue workload rate of work tasks or cases")
  protected double overdueWorkloadRate;
  @Schema(description = "Daily processed average workload of completed tasks or cases")
  protected double dailyProcessedWorkload;

  @Schema(description="The overdue time of work tasks or cases, unit hour")
  protected double overdueTime;
  @Schema(description="The time until work tasks or cases become overdue, unit hour")
  protected double overdueWorkloadProcessingTime;

  public double calcOverdueRate() {
    return calcRate(overdueNum, totalNum);
  }

  public double calcOverdueWorkloadRate() {
    return calcRate(overdueWorkload, totalWorkload);
  }

}
