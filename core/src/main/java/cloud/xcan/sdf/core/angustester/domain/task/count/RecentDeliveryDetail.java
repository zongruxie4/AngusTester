package cloud.xcan.sdf.core.angustester.domain.task.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RecentDeliveryDetail extends RecentDeliveryCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(completedNum), valueOf(completedRate),
        valueOf(totalWorkload), valueOf(completedWorkload), valueOf(completedWorkloadRate),
        valueOf(savingWorkload), valueOf(savingWorkloadRate),
        valueOf(totalOverdueNum), valueOf(overdueNum), valueOf(overdueRate),
        valueOf(overdueWorkload), valueOf(overdueWorkloadRate),
    };
  }
}
