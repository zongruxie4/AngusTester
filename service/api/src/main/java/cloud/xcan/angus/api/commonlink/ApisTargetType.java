package cloud.xcan.angus.api.commonlink;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum ApisTargetType implements EnumMessage<String> {
  SERVICE, API;

  public CombinedTargetType toCombinedType() {
    return CombinedTargetType.valueOf(this.name());
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isService() {
    return this.equals(SERVICE);
  }

  public boolean isApi() {
    return this.equals(API);
  }

}
