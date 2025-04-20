package cloud.xcan.angus.core.tester.domain.services;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ServicesExportScope implements EnumMessage<String> {
  SERVICE, APIS;

  @Override
  public String getValue() {
    return this.name();
  }

}
