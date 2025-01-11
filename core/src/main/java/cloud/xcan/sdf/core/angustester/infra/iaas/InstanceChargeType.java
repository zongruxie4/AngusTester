package cloud.xcan.sdf.core.angustester.infra.iaas;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum InstanceChargeType implements EnumMessage<String> {
  PrePaid, PostPaid;

  @Override
  public String getValue() {
    return this.name();
  }
}
