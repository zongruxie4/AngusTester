package cloud.xcan.sdf.model.apis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
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
