package cloud.xcan.sdf.core.angustester.domain.mock.apis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import lombok.Getter;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
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
