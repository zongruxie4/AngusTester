package cloud.xcan.angus.core.tester.domain.apis.share;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
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
