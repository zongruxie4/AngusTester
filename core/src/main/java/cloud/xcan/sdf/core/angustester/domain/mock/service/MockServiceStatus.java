package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
@Getter
public enum MockServiceStatus implements EnumMessage<String> {
  NOT_STARTED, RUNNING;

  @Override
  public String getValue() {
    return this.name();
  }
}
