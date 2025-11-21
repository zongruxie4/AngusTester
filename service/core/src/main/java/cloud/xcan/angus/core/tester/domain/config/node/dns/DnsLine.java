package cloud.xcan.angus.core.tester.domain.config.node.dns;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum DnsLine implements EnumMessage<String> {
  DEFAULT, TELECOM, UNICOM, MOBILE, OVERSEA/*, EDU, DRPENG, BTVN*/;

  @Override
  public String getValue() {
    return this.name();
  }
}
