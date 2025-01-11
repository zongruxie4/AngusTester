package cloud.xcan.sdf.core.angustester.domain.indicator;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum IndicatorType implements EnumMessage<String> {
  FUNC, PERF, STABILITY;

  @Override
  public String getValue() {
    return this.name();
  }
}
