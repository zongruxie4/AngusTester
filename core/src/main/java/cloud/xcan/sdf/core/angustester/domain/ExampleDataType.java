package cloud.xcan.sdf.core.angustester.domain;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ExampleDataType implements EnumMessage<String> {
  TAG, MODULE, TASK, FUNC, SERVICES, SCENARIO, SCRIPT, MOCK,
  VARIABLE, DATASET, EXECUTION, REPORT;

  @Override
  public String getValue() {
    return this.name();
  }
}
