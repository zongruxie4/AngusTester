package cloud.xcan.angus.core.tester.domain.mock.service.auth;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
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
