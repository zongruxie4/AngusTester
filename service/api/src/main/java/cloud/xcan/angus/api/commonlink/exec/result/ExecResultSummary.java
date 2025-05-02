package cloud.xcan.angus.api.commonlink.exec.result;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.apis.ApisInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecResultSummary extends ApisInfo {

  private boolean enabledTest;

  /**
   * A null value means not tested
   */
  private Boolean funcTestPassed;

  private String funcTestFailureMessage;

  /**
   * A null value means not tested
   */
  private Boolean perfTestPassed;

  private String perfTestFailureMessage;

  /**
   * A null value means not tested
   */
  private Boolean stabilityTestPassed;

  private String stabilityTestFailureMessage;

  public boolean isTested() {
    return nonNull(funcTestPassed) || nonNull(perfTestPassed) || nonNull(stabilityTestPassed);
  }

  public boolean isPassed() {
    return isTested() && (isNull(funcTestPassed) || funcTestPassed) && (isNull(perfTestPassed)
        || perfTestPassed) && (isNull(stabilityTestPassed) || stabilityTestPassed);
  }

  // For web front search
  public boolean isFailed() {
    return (nonNull(funcTestPassed) && !funcTestPassed)
        || (nonNull(perfTestPassed) && !perfTestPassed)
        || (nonNull(stabilityTestPassed) && !stabilityTestPassed);
  }

}
