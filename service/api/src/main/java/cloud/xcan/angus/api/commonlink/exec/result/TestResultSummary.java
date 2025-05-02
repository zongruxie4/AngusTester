package cloud.xcan.angus.api.commonlink.exec.result;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TestResultSummary {

  private TestResultStatus resultStatus;

  private int testNum;

  private int testFailureNum;

  private int testSuccessNum;

  private double testSuccessRate;

}
