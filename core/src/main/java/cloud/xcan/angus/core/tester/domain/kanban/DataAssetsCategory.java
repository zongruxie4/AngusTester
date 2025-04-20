package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum DataAssetsCategory implements EnumMessage<String> {
  FUNC, APIS, SCENARIO, TASK, SCRIPT, MOCK, DATA, /*,REPORT EXECUTION*/;

  @Override
  public String getValue() {
    return this.name();
  }
}
