package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import cloud.xcan.sdf.core.angustester.domain.kanban.OverdueRiskLevel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class OverdueAssessmentCount {

  @ApiModelProperty("The overdue risk level of work")
  protected OverdueRiskLevel riskLevel;

  @ApiModelProperty(value = "Total number of tasks or cases, Ignoring cancel status tasks or cases")
  protected long totalNum;
  @ApiModelProperty(value = "Total evaluate workload, Ignoring cancel status tasks or cases")
  protected double totalWorkload = 0d;

  @ApiModelProperty("The overdue number of work tasks or cases")
  protected long overdueNum;
  @ApiModelProperty("The overdue rate of work tasks or cases")
  protected double overdueRate;

  @ApiModelProperty("The overdue workload of work tasks or cases")
  protected double overdueWorkload;
  @ApiModelProperty("The overdue workload rate of work tasks or cases")
  protected double overdueWorkloadRate;
  @ApiModelProperty(value = "Daily processed average workload of completed tasks or cases")
  protected double dailyProcessedWorkload;

  @ApiModelProperty("The overdue time of work tasks or cases, unit hour")
  protected double overdueTime;
  @ApiModelProperty("The time until work tasks or cases become overdue, unit hour")
  protected double overdueWorkloadProcessingTime;

  public double calcOverdueRate() {
    return calcRate(overdueNum, totalNum);
  }

  public double calcOverdueWorkloadRate() {
    return calcRate(overdueWorkload, totalWorkload);
  }

}
