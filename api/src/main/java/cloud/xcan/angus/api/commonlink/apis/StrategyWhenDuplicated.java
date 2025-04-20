package cloud.xcan.angus.api.commonlink.apis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
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
