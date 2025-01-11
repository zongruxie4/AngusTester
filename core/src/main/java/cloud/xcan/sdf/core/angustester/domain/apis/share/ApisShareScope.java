package cloud.xcan.sdf.core.angustester.domain.apis.share;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum ApisShareScope implements EnumMessage<String> {
  SERVICES, PARTIAL_APIS, SINGLE_APIS;

  public CombinedTargetType toCombinedType() {
    return CombinedTargetType.valueOf(this.name());
  }

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isService() {
    return this.equals(SERVICES);
  }

  public boolean isPartialApi() {
    return this.equals(PARTIAL_APIS);
  }

  public boolean isSingleApi() {
    return this.equals(SINGLE_APIS);
  }

}
