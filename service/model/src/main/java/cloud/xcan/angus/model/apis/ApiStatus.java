package cloud.xcan.angus.model.apis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ApiStatus implements EnumMessage<String> {
  UNKNOWN, IN_DESIGN, IN_DEV, DEV_COMPLETED, RELEASED;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isReleased() {
    return this.equals(RELEASED);
  }
}
