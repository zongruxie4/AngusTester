package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum BurnDownResourceType implements EnumMessage<String> {
  NUM, WORKLOAD;

  @Override
  public String getValue() {
    return this.name();
  }
}
