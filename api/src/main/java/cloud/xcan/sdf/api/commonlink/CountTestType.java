package cloud.xcan.sdf.api.commonlink;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
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
