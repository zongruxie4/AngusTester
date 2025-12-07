package cloud.xcan.angus.api.commonlink.script;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ScriptPermission implements EnumMessage<String> {
  VIEW, MODIFY, DELETE, TEST, GRANT, EXPORT;

  public static final List<ScriptPermission> ALL = List.of(VIEW, MODIFY, DELETE, TEST, GRANT,
      EXPORT);

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }
}
