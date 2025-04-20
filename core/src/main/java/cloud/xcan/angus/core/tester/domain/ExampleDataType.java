package cloud.xcan.angus.core.tester.domain;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ExampleDataType implements EnumMessage<String> {
  TAG, MODULE, TASK, FUNC, SERVICES, SCENARIO, SCRIPT, MOCK,
  VARIABLE, DATASET, EXECUTION, REPORT;

  @Override
  public String getValue() {
    return this.name();
  }
}
