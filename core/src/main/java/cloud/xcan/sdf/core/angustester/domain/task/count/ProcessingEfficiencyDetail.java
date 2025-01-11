package cloud.xcan.sdf.core.angustester.domain.task.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProcessingEfficiencyDetail extends ProcessingEfficiencyCount
    implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum),
        valueOf(completedNum), valueOf(completedRate),
        valueOf(oneTimePassedNum), valueOf(oneTimePassedRate),
        valueOf(twoTimePassedNum), valueOf(twoTimePassedRate),
        valueOf(oneTimeNotPassedNum), valueOf(oneTimeNotPassedRate)
    };
  }
}
