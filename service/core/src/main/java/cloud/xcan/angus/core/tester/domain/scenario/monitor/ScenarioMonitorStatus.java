package cloud.xcan.angus.core.tester.domain.scenario.monitor;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ScenarioMonitorStatus implements EnumMessage<String> {
  PENDING, SUCCESS, FAILURE;

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }

  public boolean isFailure() {
    return this.equals(FAILURE);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
