package cloud.xcan.angus.core.tester.domain.func.cases;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
public enum CaseStepView implements EnumMessage<String> {
  TABLE, TEXT;

  public static CaseStepView DEFAULT = TABLE;

  @Override
  public String getValue() {
    return this.name();
  }

}
