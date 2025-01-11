package cloud.xcan.sdf.core.angustester.domain.services;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ServicesExportScope implements EnumMessage<String> {
  SERVICE, APIS;

  @Override
  public String getValue() {
    return this.name();
  }

}
