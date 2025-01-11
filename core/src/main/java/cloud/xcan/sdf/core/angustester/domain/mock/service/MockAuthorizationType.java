package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum MockAuthorizationType implements EnumMessage<String> {
  HTTP_BASIC, REQUEST_HEADER_MATCH;

  @Override
  public String getValue() {
    return this.name();
  }
}
