package cloud.xcan.angus.core.tester.domain.project.evaluation;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EvaluationPurpose implements EnumMessage<String> {

  FUNCTIONAL_PASSED_RATE,

  PERFORMANCE_PASSED_RATE,

  STABILITY_PASSED_RATE,

  SECURITY_SCORE,

  COMPATIBILITY_SCORE,

  USABILITY_SCORE,

  MAINTAINABILITY_SCORE,

  SCALABILITY_SCORE;

  @Override
  public String getValue() {
    return this.name();
  }
}
