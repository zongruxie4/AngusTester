package cloud.xcan.angus.api.commonlink.node;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
@Getter
public enum NodeSource implements EnumMessage<String> {
  OWN_NODE,
  ONLINE_BUY;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isOwnNode() {
    return this.equals(OWN_NODE);
  }

  public boolean isOnlineBuy() {
    return this.equals(ONLINE_BUY);
  }
}
