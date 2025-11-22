package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ReportCategory implements EnumMessage<String> {
  PROJECT, TASK, FUNCTIONAL, APIS, SCENARIO, EXECUTION;

  @Override
  public String getValue() {
    return this.name();
  }
}
