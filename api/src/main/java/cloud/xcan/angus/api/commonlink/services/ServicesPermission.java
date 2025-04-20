package cloud.xcan.angus.api.commonlink.services;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ServicesPermission implements EnumMessage<String> {
  ADD, VIEW, MODIFY, DELETE, /*DEBUG, */TEST, GRANT, SHARE, RELEASE, EXPORT;

  public static final List<ServicesPermission> ALL = List
      .of(ADD, VIEW, MODIFY, DELETE, /*DEBUG, */TEST, GRANT, SHARE, RELEASE, EXPORT);

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isRelease() {
    return this.equals(RELEASE);
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }
}
