package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum BurnDownResourceType implements EnumMessage<String> {
  NUM, WORKLOAD;

  @Override
  public String getValue() {
    return this.name();
  }
}
