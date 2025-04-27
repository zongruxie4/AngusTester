package cloud.xcan.angus.core.tester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.angus.core.tester.domain.task.count.DataDetailBase;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestingEfficiencyDetail extends TestingEfficiencyCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum),
        valueOf(passedTestNum), valueOf(passedTestRate),
        valueOf(oneTimePassedNum), valueOf(oneTimePassedRate),
        valueOf(twoTimePassedNum), valueOf(twoTimePassedRate),
        valueOf(oneTimeNotPassedNum), valueOf(oneTimeNotPassedRate)
    };
  }
}
