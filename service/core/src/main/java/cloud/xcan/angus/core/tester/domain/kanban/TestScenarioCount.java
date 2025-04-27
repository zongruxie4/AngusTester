package cloud.xcan.angus.core.tester.domain.kanban;

import static cloud.xcan.angus.spec.utils.ObjectUtils.calcRate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestScenarioCount {

  private long totalScenarioNum;
  private long enabledTestScenarioNum;
  private double enabledTestScenarioRate;
  private long passedTestScenarioNum;
  private double testScenarioProgress;

  private long enabledTestNum;
  private long passedTestNum;
  private double testProgress;

  public double calcEnabledTestScenarioRate() {
    return calcRate(enabledTestScenarioNum, totalScenarioNum);
  }

  public double calcTestScenarioProgress() {
    return calcRate(passedTestScenarioNum, enabledTestScenarioNum);
  }

  public double calcTestProgress() {
    return calcRate(passedTestNum, enabledTestNum);
  }

}
