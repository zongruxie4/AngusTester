package cloud.xcan.sdf.core.angustester.domain.func.cases;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum CaseStepView implements EnumMessage<String> {
  TABLE, TEXT;

  public static CaseStepView DEFAULT = TABLE;

  @Override
  public String getValue() {
    return this.name();
  }

}
