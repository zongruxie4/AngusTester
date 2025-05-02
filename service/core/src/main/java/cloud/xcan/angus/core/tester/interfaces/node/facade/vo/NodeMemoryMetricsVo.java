package cloud.xcan.angus.core.tester.interfaces.node.facade.vo;

import static java.util.Objects.nonNull;

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
public class NodeMemoryMetricsVo {

  @JsonIgnore
  private Long timestamp0;

  /**
   * @see Memory#toString()
   */
  @Schema(description="Comma-separated memory sample value, format: free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed")
  private String cvsMemory;

  public Date getTimestamp() {
    return nonNull(timestamp0) ? new Date(timestamp0) : null;
  }

}
