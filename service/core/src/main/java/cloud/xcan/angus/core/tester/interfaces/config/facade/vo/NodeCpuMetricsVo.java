package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.NodeUsage.Cpu;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeCpuMetricsVo {

  @JsonIgnore
  private Long timestamp0;

  /**
   * @see Cpu#toString()
   */
  @Schema(description="Comma-separated cpu sample value, format: idle,sys,user,wait,other,total")
  private String cvsCpu;

  public Date getTimestamp() {
    return nonNull(timestamp0) ? new Date(timestamp0) : null;
  }
}
