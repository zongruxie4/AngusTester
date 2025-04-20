package cloud.xcan.angus.core.tester.domain.mock;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */@EndpointRegister
public enum MockDataTargetType implements EnumMessage<String> {
  FILE, DATASOURCE;

  @Override
  public String getValue() {
    return this.name();
  }

}
