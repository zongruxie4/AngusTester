package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeNetworkMetricsVo.NetworkValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ModeNetworkInfoVo {

  private String deviceName;

  private NetworkValue networkUsage;

}
