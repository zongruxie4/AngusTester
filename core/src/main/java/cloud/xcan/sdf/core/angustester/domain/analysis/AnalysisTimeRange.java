package cloud.xcan.sdf.core.angustester.domain.analysis;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum AnalysisTimeRange implements EnumMessage<String> {
  ALL_TIME,
  THIS_WEEK,
  LAST_WEEK,
  THIS_MONTH,
  LAST_MONTH,
  THIS_YEAR,
  CUSTOM_TIME;

  @Override
  public String getValue() {
    return this.name();
  }
}
