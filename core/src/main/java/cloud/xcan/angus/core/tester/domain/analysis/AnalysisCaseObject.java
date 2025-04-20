package cloud.xcan.angus.core.tester.domain.analysis;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum AnalysisCaseObject implements EnumMessage<String> {
  CURRENT_PROJECT, PLAN, TESTER_ORG;

  @Override
  public String getValue() {
    return this.name();
  }
}
