package cloud.xcan.angus.core.tester.domain.report.auth;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
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
