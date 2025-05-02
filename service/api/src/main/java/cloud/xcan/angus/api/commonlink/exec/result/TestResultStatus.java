package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

@Getter
@EndpointRegister
public enum TestResultStatus implements EnumMessage<String> {
  NOT_ENABLED, UNTESTED, PARTIALLY_PASSED, FULLY_PASSED, FULLY_FAILED;

  @Override
  public String getValue() {
    return this.name();
  }
}
