package cloud.xcan.angus.core.tester.domain.project.evaluation;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EvaluationScope implements EnumMessage<String> {
  PROJECT, FUNC_PLAN, MODULE;

  @Override
  public String getValue() {
    return this.name();
  }
}
