package cloud.xcan.sdf.core.angustester.domain.mock;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum MockDataTargetType implements EnumMessage<String> {
  FILE, DATASOURCE;

  @Override
  public String getValue() {
    return this.name();
  }

}
