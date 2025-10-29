package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.Locale;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum CaseTestResult implements EnumMessage<String> {
  PENDING, PASSED, NOT_PASSED, BLOCKED, CANCELED;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPassed() {
    return this.equals(PASSED);
  }

  public boolean isFinished(){
    return this.equals(PASSED) || this.equals(NOT_PASSED);
  }

  public boolean isWideFinished(){
    return this.equals(PASSED) || this.equals(NOT_PASSED) || this.equals(CANCELED);
  }

  public boolean isNotPassed() {
    return this.equals(NOT_PASSED);
  }

  public boolean isTestAction() {
    return this.equals(PASSED) || this.equals(NOT_PASSED);
  }

  public boolean isCanceled() {
    return this.equals(CANCELED);
  }

  public boolean canAutoUpdateResult() {
    return this.equals(PENDING) || this.equals(PASSED) || this.equals(NOT_PASSED);
  }

  public static CaseTestResult ofMessage(String testStatusMassage, Locale locale) {
    for (CaseTestResult value : values()) {
      String message = MessageHolder.message(value.getMessageKey(), locale);
      if (message.equals(testStatusMassage.trim())) {
        return value;
      }
    }
    throw new ProtocolException("Unknown test result: " + testStatusMassage);
  }


}
