package cloud.xcan.angus.core.tester.domain.indicator;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum IndicatorType implements EnumMessage<String> {
  FUNC, PERF, STABILITY;

  @Override
  public String getValue() {
    return this.name();
  }
}
