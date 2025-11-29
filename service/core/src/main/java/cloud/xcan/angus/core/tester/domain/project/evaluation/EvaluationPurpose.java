package cloud.xcan.angus.core.tester.domain.project.evaluation;


import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum EvaluationPurpose implements EnumMessage<String> {

  FUNCTIONAL_SCORE,

  PERFORMANCE_SCORE,

  STABILITY_SCORE,

  SECURITY_SCORE,

  COMPATIBILITY_SCORE,

  COMPLIANCE_SCORE,

  AVAILABILITY_SCORE,

  USABILITY_SCORE,

  MAINTAINABILITY_SCORE,

  SCALABILITY_SCORE;

  @Override
  public String getValue() {
    return this.name();
  }
}
