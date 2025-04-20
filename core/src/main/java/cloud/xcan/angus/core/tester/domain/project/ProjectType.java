package cloud.xcan.angus.core.tester.domain.project;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ProjectType implements EnumMessage<String> {
  AGILE, GENERAL, TESTING;

  public static final ProjectType DEFAULT = AGILE;

  public boolean isAgile() {
    return this == AGILE;
  }

  public boolean isGeneral() {
    return this == GENERAL;
  }

  public boolean isTesting() {
    return this == TESTING;
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
