package cloud.xcan.sdf.core.angustester.domain.kanban;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TotalProgressCount {

  private double totalProgress;

  private long totalNum;
  private long totalCompletedNum;
  private double totalCompletedRate;

  private double totalWorkload;
  private double totalCompletedWorkload;
  private double totalCompletedWorkloadRate;

  public double calcTotalCompletedRate() {
    return calcRate(totalCompletedNum, totalNum);
  }

  public double calcTotalCompletedWorkloadRate() {
    return calcRate(totalCompletedWorkload, totalWorkload);
  }
}
