package cloud.xcan.sdf.core.angustester.domain.report.auth;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum ReportPermission implements EnumMessage<String> {
  VIEW, MODIFY, GENERATE, DELETE, GRANT, /*@Deprecated SHARE,*/ EXPORT;

  public static List<ReportPermission> ALL = List.of(
      VIEW, MODIFY, GENERATE, DELETE, GRANT, /*SHARE,*/ EXPORT
  );

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }

}
