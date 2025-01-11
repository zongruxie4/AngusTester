package cloud.xcan.sdf.core.angustester.domain.task.count;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentDeliveryCount {

  @ApiModelProperty(value = "The number of total work")
  protected long totalNum;
  @ApiModelProperty(value = "The number of completed work")
  protected long completedNum;
  @ApiModelProperty(value = "The rate of completed work")
  protected double completedRate = 0d;

  @ApiModelProperty(value = "Total workload")
  protected double totalWorkload;
  @ApiModelProperty(value = "Completed workload")
  protected double completedWorkload = 0d;
  @ApiModelProperty(value = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;
  @ApiModelProperty(value = "Saving workload")
  protected double savingWorkload = 0d;
  @ApiModelProperty(value = "The rate of saving workload")
  protected double savingWorkloadRate = 0d;

  @ApiModelProperty(value = "The number of total overdue")
  protected long totalOverdueNum;
  @ApiModelProperty(value = "The number of overdue")
  protected long overdueNum;
  @ApiModelProperty(value = "Overdue rate")
  protected double overdueRate = 0d;
  @ApiModelProperty(value = "Overdue workload")
  protected double overdueWorkload = 0d;
  @ApiModelProperty(value = "The rate of overdue workload")
  protected double overdueWorkloadRate = 0d;

  public double calcCompletedRate() {
    return calcRate(completedNum, totalNum);
  }

  public double calcCompletedWorkloadRate() {
    return calcRate(completedWorkload, totalWorkload);
  }

  public double calcSavingWorkloadRate() {
    return calcRate(savingWorkload, totalWorkload);
  }

  public double calcOverdueRate() {
    return calcRate(overdueNum, totalNum);
  }

  public double calcOverdueWorkloadRate() {
    return calcRate(overdueWorkload, totalWorkload);
  }

}
