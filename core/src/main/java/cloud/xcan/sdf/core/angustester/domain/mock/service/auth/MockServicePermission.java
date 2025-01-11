package cloud.xcan.sdf.core.angustester.domain.mock.service.auth;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum MockServicePermission implements EnumMessage<String> {
  ADD, VIEW, MODIFY, DELETE, RUN, GRANT, EXPORT;

  public static List<MockServicePermission> ALL = List
      .of(ADD, VIEW, MODIFY, DELETE, RUN, GRANT, EXPORT);

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }
}
