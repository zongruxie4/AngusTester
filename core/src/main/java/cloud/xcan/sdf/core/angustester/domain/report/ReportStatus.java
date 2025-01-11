package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ReportStatus implements EnumMessage<String> {

  PENDING, SUCCESS, FAILURE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isSuccess() {
    return this.equals(SUCCESS);
  }
}
