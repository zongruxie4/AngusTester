package cloud.xcan.angus.core.tester.domain.mock.service;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
@Getter
public enum MockServiceStatus implements EnumMessage<String> {
  NOT_STARTED, RUNNING;

  @Override
  public String getValue() {
    return this.name();
  }
}
