package cloud.xcan.angus.api.commonlink.apis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ApiSource implements EnumMessage<String> {
  CREATED,
  /**
   * Add from editor.
   */
  EDITOR,
  IMPORT,
  /**
   * Only OpenAPI(Swagger) supported.
   */
  SYNC;

  @Override
  public String getValue() {
    return this.name();
  }
}
