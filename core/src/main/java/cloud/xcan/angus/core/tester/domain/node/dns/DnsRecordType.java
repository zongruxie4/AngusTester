package cloud.xcan.angus.core.tester.domain.node.dns;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;

/**
 * @author wjl
 */
@EndpointRegister
public enum DnsRecordType implements EnumValueMessage<String> {
  A, NS, MX, TXT, CNAME, SRV, AAAA, CAA/*, REDIRECT_URL, FORWARD_URL*/;

  @Override
  public String getValue() {
    return this.name();
  }
}
