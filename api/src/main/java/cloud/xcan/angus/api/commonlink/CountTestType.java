package cloud.xcan.angus.api.commonlink;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

@EndpointRegister
@Getter
public enum CountTestType implements EnumMessage<String> {
  PERF, FUNCTIONAL, STABILITY, ALL;

  @Override
  public String getValue() {
    return this.name();
  }
}
