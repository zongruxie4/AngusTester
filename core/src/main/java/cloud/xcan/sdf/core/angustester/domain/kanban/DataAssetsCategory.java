package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum DataAssetsCategory implements EnumMessage<String> {
  FUNC, APIS, SCENARIO, TASK, SCRIPT, MOCK, DATA, REPORT/*, EXECUTION*/;

  @Override
  public String getValue() {
    return this.name();
  }
}
