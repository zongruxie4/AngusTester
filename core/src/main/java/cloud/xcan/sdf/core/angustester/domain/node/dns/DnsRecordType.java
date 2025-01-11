package cloud.xcan.sdf.core.angustester.domain.node.dns;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumValueMessage;

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
