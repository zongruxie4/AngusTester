package cloud.xcan.angus.core.tester.domain.issue.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkloadDetail extends WorkloadCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(evalWorkload), valueOf(actualWorkload),
        valueOf(completedWorkload), valueOf(completedWorkloadRate),
        valueOf(savingWorkload), valueOf(savingWorkloadRate),
        valueOf(invalidWorkload), valueOf(invalidWorkloadRate),
    };
  }
}
