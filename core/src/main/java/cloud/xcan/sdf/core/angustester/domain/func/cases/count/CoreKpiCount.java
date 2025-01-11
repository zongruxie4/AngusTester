package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiCount {

  @ApiModelProperty(value = "Total number of cases, Ignoring cancel status cases")
  protected long totalNum;
  @ApiModelProperty(value = "The number of completed cases")
  protected long completedNum;
  @ApiModelProperty(value = "The rate of completed cases")
  protected double completedRate;

  @ApiModelProperty(value = "Evaluate workload")
  protected double evalWorkload = 0d;
  @ApiModelProperty(value = "Completed workload")
  protected double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;

  @ApiModelProperty(value = "The number of overdue cases")
  protected long overdueNum;
  @ApiModelProperty(value = "The rate of overdue cases")
  protected double overdueRate = 0d;
  @ApiModelProperty(value = "The number of completed overdue cases")
  protected long completedOverdueNum;
  @ApiModelProperty(value = "The rate of completed overdue cases")
  protected double completedOverdueRate = 0d;

  @ApiModelProperty(value = "The number of review cases")
  protected long reviewNum;
  @ApiModelProperty(value = "The rate of review cases")
  protected double reviewRate = 0d;
  @ApiModelProperty(value = "The number of completed review cases")
  protected long completedReviewNum;
  @ApiModelProperty(value = "The rate of completed review cases")
  protected double completedReviewRate = 0d;

}
