package cloud.xcan.angus.api.commonlink.associate;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum AssociateUserType implements EnumMessage<String> {
  CREATOR, ASSIGNEE, CONFIRMOR, OWNER, SYS_ADMIN, APP_ADMIN;

  @Override
  public String getValue() {
    return this.name();
  }
}
