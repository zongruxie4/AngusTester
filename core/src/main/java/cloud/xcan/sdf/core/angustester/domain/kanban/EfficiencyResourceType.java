package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum EfficiencyResourceType implements EnumMessage<String> {
  TASK, CASE;

  @Override
  public String getValue() {
    return this.name();
  }
}
