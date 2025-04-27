package cloud.xcan.angus.core.tester.domain.func.plan.auth;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import java.util.List;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
public enum FuncPlanPermission implements EnumMessage<String> {
  MODIFY_PLAN, DELETE_PLAN, ADD_CASE, MODIFY_CASE, DELETE_CASE, EXPORT_CASE,
  REVIEW, RESET_REVIEW_RESULT, TEST, RESET_TEST_RESULT, ESTABLISH_BASELINE, GRANT;

  public static List<FuncPlanPermission> ALL = List.of(
      MODIFY_PLAN, DELETE_PLAN, ADD_CASE, MODIFY_CASE, DELETE_CASE, EXPORT_CASE,
      REVIEW, RESET_REVIEW_RESULT, TEST, RESET_TEST_RESULT, ESTABLISH_BASELINE, GRANT
  );

  public static List<FuncPlanPermission> TESTER = List.of(
      ADD_CASE, MODIFY_CASE, DELETE_CASE, EXPORT_CASE, TEST, RESET_TEST_RESULT
  );

  public boolean notIgnorePublicAccess() {
    return this.equals(RESET_REVIEW_RESULT) || this.equals(RESET_TEST_RESULT) || this.equals(GRANT);
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isGrant() {
    return this.equals(GRANT);
  }

}
