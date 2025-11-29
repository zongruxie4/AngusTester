package cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting;

import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxyType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UserApiProxyEnabledDto implements Serializable {

  @NotNull
  @Schema(description = "API proxy service type to enable for user access", requiredMode = RequiredMode.REQUIRED)
  private ApiProxyType name;

}
