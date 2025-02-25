package cloud.xcan.sdf.core.angustester.domain.apis.design;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ApisDesignSource implements EnumMessage<String> {
  SYNCHRONOUS_SERVICE, FILE_IMPORT, MANUAL_CREATED;

  public boolean isSynchronousService() {
    return this == SYNCHRONOUS_SERVICE;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
