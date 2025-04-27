package cloud.xcan.angus.core.tester.domain.mock.service;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
public enum MockAuthorizationType implements EnumMessage<String> {
  HTTP_BASIC, REQUEST_HEADER_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }
}
