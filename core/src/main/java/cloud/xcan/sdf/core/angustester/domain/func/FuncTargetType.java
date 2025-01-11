package cloud.xcan.sdf.core.angustester.domain.func;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

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
