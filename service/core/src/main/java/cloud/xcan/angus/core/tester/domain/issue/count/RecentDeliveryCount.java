package cloud.xcan.angus.core.tester.domain.issue.count;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentDeliveryCount {

  @Schema(description = "The number of total work")
  protected long totalNum;
  @Schema(description = "The number of completed work")
  protected long completedNum;
  @Schema(description = "The rate of completed work")
  protected double completedRate = 0d;

  @Schema(description = "Total workload")
  protected double totalWorkload;
  @Schema(description = "Completed workload")
  protected double completedWorkload = 0d;
  @Schema(description = "The rate of completed workload")
  protected double completedWorkloadRate = 0d;
  @Schema(description = "Saving workload")
  protected double savingWorkload = 0d;
  @Schema(description = "The rate of saving workload")
  protected double savingWorkloadRate = 0d;

  @Schema(description = "The number of total overdue")
  protected long totalOverdueNum;
  @Schema(description = "The number of overdue")
  protected long overdueNum;
  @Schema(description = "Overdue rate")
  protected double overdueRate = 0d;
  @Schema(description = "Overdue workload")
  protected double overdueWorkload = 0d;
  @Schema(description = "The rate of overdue workload")
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
