package cloud.xcan.angus.core.tester.domain.apis.design;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

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
