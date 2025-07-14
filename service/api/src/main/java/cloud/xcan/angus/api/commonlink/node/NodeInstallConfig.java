package cloud.xcan.angus.api.commonlink.node;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeInstallConfig {

  private Long tenantId;

  private Long deviceId;

  private String serverCtrlUrlPrefix;

  private String ctrlAccessToken;

}
