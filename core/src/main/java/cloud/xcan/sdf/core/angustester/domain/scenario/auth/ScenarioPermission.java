package cloud.xcan.sdf.core.angustester.domain.scenario.auth;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
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
