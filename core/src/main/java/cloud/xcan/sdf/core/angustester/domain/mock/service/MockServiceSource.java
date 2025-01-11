package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author xiaolong.liu
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
