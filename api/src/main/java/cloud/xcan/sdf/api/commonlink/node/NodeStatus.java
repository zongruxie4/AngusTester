package cloud.xcan.sdf.api.commonlink.node;

import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * Unused.
 *
 * @author xiaolong.liu
 */
//@EndpointRegister
@Getter
public enum NodeStatus implements EnumMessage<String> {
  PENDING,
  STARTING,
  RUNNING,
  STOPPING,
  STOPPED;

  @Override
  public String getValue() {
    return this.name();
  }
}
