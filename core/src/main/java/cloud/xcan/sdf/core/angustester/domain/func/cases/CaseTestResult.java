package cloud.xcan.sdf.core.angustester.domain.func.cases;

import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import java.util.Locale;

/**
 * @author xiaolong.liu
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
    throw new CommProtocolException("Unknown test result: " + testStatusMassage);
  }


}
