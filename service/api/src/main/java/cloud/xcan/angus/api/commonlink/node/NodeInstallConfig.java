package cloud.xcan.angus.api.commonlink.node;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeInstallConfig {

  private String ctrlAccessToken;

  private Long tenantId;

  private Long deviceId;

}
