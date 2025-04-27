package cloud.xcan.angus.core.tester.infra.iaas;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum InstanceChargeType implements EnumMessage<String> {
  PrePaid, PostPaid;

  @Override
  public String getValue() {
    return this.name();
  }
}
