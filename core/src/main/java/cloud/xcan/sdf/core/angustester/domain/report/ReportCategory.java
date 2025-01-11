package cloud.xcan.sdf.core.angustester.domain.report;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

@EndpointRegister
public enum ReportCategory implements EnumMessage<String> {
  PROJECT, TASK, FUNCTIONAL, APIS, SCENARIO, EXECUTION;

  @Override
  public String getValue() {
    return this.name();
  }
}
