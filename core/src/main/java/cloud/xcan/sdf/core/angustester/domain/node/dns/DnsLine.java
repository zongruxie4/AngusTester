package cloud.xcan.sdf.core.angustester.domain.node.dns;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum DnsLine implements EnumMessage<String> {
  DEFAULT, TELECOM, UNICOM, MOBILE, OVERSEA/*, EDU, DRPENG, BTVN*/;

  @Override
  public String getValue() {
    return this.name();
  }
}
