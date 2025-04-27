package cloud.xcan.angus.api.commonlink.apis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ApiPermission implements EnumMessage<String> {
  VIEW, MODIFY, DELETE, DEBUG, TEST, GRANT, SHARE, RELEASE, EXPORT;

  public static final List<ApiPermission> ALL = List
      .of(VIEW, MODIFY, DELETE, DEBUG, TEST, GRANT, SHARE, RELEASE, EXPORT);

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
