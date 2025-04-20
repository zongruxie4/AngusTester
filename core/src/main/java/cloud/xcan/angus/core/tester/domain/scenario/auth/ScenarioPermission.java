package cloud.xcan.angus.core.tester.domain.scenario.auth;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
public enum ScenarioPermission implements EnumMessage<String> {
  VIEW, MODIFY, DELETE, TEST, GRANT, EXPORT;

  public static List<ScenarioPermission> ALL = List.of(VIEW, MODIFY, DELETE, TEST, GRANT, EXPORT);

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }
}
