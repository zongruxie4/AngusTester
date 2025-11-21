package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.NodeDiskMetricsVo.DiskValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeDiskInfoVo {

  private String deviceName;

  private DiskValue diskUsage;

}
