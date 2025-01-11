package cloud.xcan.sdf.core.angustester.domain.kanban;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.calcRate;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TestApisCount {

  private long totalApiNum;
  private long enabledTestApiNum;
  private double enabledTestApiRate;
  private long passedTestApiNum;
  private double testApiProgress;

  private long enabledTestNum;
  private long passedTestNum;
  private double testProgress;

  public double calcEnabledTestApiRate() {
    return calcRate(enabledTestApiNum, totalApiNum);
  }

  public double calcTestApiProgress() {
    return calcRate(passedTestApiNum, enabledTestApiNum);
  }

  public double calcTestProgress() {
    return calcRate(passedTestNum, enabledTestNum);
  }

}
