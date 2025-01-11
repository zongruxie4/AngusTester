package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ScenarioMonitorStatus implements EnumMessage<String> {
  PENDING, SUCCESS, FAILURE;

  public boolean isSuccess(){
    return this.equals(SUCCESS);
  }

  public boolean isFailure(){
    return this.equals(FAILURE);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
