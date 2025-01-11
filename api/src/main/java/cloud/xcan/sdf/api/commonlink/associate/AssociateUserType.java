package cloud.xcan.sdf.api.commonlink.associate;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum AssociateUserType implements EnumMessage<String> {
  CREATOR, ASSIGNEE, CONFIRMOR, OWNER, SYS_ADMIN, APP_ADMIN;

  @Override
  public String getValue() {
    return this.name();
  }
}
