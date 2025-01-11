package cloud.xcan.sdf.api.commonlink;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
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
