package cloud.xcan.sdf.api.commonlink.apis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum ApiPermission implements EnumMessage<String> {
  VIEW, MODIFY, DELETE, DEBUG, TEST, GRANT, SHARE, RELEASE, EXPORT;

  public static List<ApiPermission> ALL = List
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
