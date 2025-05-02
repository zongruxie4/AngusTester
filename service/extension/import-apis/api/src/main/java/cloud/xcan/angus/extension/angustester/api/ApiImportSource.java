package cloud.xcan.angus.extension.angustester.api;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ApiImportSource implements EnumMessage<String> {
  OPENAPI, /*YAPI,*/ POSTMAN, ANGUS;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isAngus() {
    return this.equals(ANGUS);
  }

  public boolean isWideOpenapi() {
    return this.equals(OPENAPI) || this.equals(POSTMAN) /* Processed by the web front-end. */;
  }

}
