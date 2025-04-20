package cloud.xcan.angus.api.commonlink.node;

import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * Unused.
 *
 * @author XiaoLong Liu
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
