package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AnalysisTaskObject implements EnumMessage<String> {
  CURRENT_PROJECT, SPRINT, ASSIGNEE_ORG;

  @Override
  public String getValue() {
    return this.name();
  }
}
