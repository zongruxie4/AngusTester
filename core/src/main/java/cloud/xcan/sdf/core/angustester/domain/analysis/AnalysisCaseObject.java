package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AnalysisCaseObject implements EnumMessage<String> {
  CURRENT_PROJECT, PLAN, TESTER_ORG;

  @Override
  public String getValue() {
    return this.name();
  }
}
