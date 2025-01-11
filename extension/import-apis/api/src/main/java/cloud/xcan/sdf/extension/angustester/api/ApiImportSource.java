package cloud.xcan.sdf.extension.angustester.api;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
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

}
