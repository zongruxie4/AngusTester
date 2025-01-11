package cloud.xcan.sdf.api.commonlink.node;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author xiaolong.liu
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
