package cloud.xcan.angus.core.tester.domain;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum GlobalResourceType implements EnumMessage<String> {
  FUNC, PERF, STABILITY, EVALUATION;

  @Override
  public String getValue() {
    return this.name();
  }
}
