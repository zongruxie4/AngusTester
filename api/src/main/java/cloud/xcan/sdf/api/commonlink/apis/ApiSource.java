package cloud.xcan.sdf.api.commonlink.apis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
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
