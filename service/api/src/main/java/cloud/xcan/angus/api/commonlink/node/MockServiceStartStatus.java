package cloud.xcan.angus.api.commonlink.node;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

@EndpointRegister
@Getter
public enum MockServiceStartStatus implements EnumMessage<String> {
  NOT_STARTED, SUCCESS, FAILURE, UNKNOWN;

  @Override
  public String getValue() {
    return this.name();
  }
}
