package cloud.xcan.angus.core.tester.interfaces.node.facade.vo;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Cpu;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.FileSystem;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Memory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeMetricsVo {

  @JsonIgnore
  private Long timestamp0;

  /**
   * @see Cpu#toString()
   */
  @Schema(description="Comma-separated cpu sample value, format: idle,sys,user,wait,other,total")
  private String cvsCpu;

  /**
   * @see Memory#toString()
   */
  @Schema(description="Comma-separated memory sample value, format: free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed")
  private String cvsMemory;

  /**
   * @see FileSystem#toString()
   */
  @Schema(description="Comma-separate filesystem sample value, format: free,used,avail,usePercent")
  private String cvsFilesystem;

  public Date getTimestamp() {
    return nonNull(timestamp0) ? new Date(timestamp0) : null;
  }
}
