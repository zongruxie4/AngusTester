package cloud.xcan.sdf.api.commonlink.apis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum StrategyWhenDuplicated implements EnumMessage<String> {

  COVER,
  IGNORE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isCover() {
    return this.equals(COVER);
  }

}
