package cloud.xcan.sdf.api.commonlink.services;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum ServicesPermission implements EnumMessage<String> {
  ADD, VIEW, MODIFY, DELETE, /*DEBUG, */TEST, GRANT, SHARE, RELEASE, EXPORT;

  public static List<ServicesPermission> ALL = List
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
