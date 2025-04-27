package cloud.xcan.angus.core.tester.domain.mock.apis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
@Getter
public enum MockApisSource implements EnumMessage<String> {
  /**
   * Manually create a mock apis.
   */
  CREATED,
  /**
   * Imported by angus files.
   */
  ANGUS_IMPORT,
  /**
   * Copy an existing apis.
   */
  COPY_APIS,
  /**
   * Associate an existing apis.
   */
  ASSOC_APIS;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isAssociatedApis() {
    return this.equals(ASSOC_APIS);
  }
}
