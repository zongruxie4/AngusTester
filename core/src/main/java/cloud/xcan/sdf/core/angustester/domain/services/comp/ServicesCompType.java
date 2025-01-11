package cloud.xcan.sdf.core.angustester.domain.services.comp;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import lombok.Getter;

@EndpointRegister
@Getter
public enum ServicesCompType implements EnumMessage<String> {
  schemas, responses, parameters,
  examples, requestBodies, headers,
  securitySchemes, links, callbacks, extensions, pathItems;

  public boolean isSecuritySchemes(){
    return this.equals(securitySchemes);
  }

  @Override
  public String getValue() {
    return this.name();
  }

}
