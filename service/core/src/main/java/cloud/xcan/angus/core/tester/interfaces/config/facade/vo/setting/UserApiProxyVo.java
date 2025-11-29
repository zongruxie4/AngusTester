package cloud.xcan.angus.core.tester.interfaces.config.facade.vo.setting;

import cloud.xcan.angus.core.tester.domain.config.user.apiproxy.ApiProxy;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class UserApiProxyVo implements Serializable {

  @Schema(description = "Configuration without proxy.")
  private ApiProxy noProxy;

  @Schema(description = "Configuration of client proxy.")
  private ApiProxy clientProxy;

  @Schema(description = "Configuration of server proxy.")
  private ApiProxy serverProxy;

  @Schema(description = "Configuration of cloud proxy.")
  private ApiProxy cloudProxy;

}
