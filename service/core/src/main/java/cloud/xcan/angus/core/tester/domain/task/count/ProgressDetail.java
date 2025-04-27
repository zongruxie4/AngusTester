package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressDetail extends ProgressCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(completedNum), valueOf(completedRate),
        valueOf(evalWorkload), valueOf(completedWorkload), valueOf(completedWorkloadRate)
    };
  }
}
