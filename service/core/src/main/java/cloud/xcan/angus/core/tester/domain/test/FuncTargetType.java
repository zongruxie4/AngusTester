package cloud.xcan.angus.core.tester.domain.test;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum FuncTargetType implements EnumMessage<String> {
  PLAN, CASE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPlan() {
    return this.equals(PLAN);
  }

  public boolean isCase() {
    return this.equals(CASE);
  }
}
