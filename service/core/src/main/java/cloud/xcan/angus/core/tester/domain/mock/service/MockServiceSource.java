package cloud.xcan.angus.core.tester.domain.mock.service;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
@Getter
public enum MockServiceSource implements EnumMessage<String> {
  /**
   * Manually create a mock service
   */
  CREATED,
  /**
   * File Import Mock
   */
  FILE_IMPORT,
  /**
   * Associate an existing services
   */
  ASSOC_SERVICE;

  @Override
  public String getValue() {
    return this.name();
  }
}
