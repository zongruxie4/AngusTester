package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage.JvmMemory;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.jvmservice.JvmServiceUsage.JvmProcessor;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class MockServiceMetricsVo {

  private Long timestamp0;

  /**
   * @see JvmMemory#toString()
   */
  @Schema(description = "Comma-separated mock service JVM sample value, format: bufferCount,bufferMemoryUsed,bufferTotalCapacity,memoryUsed,memoryCommitted,memoryMax")
  private String cvsJvm;

  /**
   * @see JvmProcessor#toString()
   */
  @Schema(description = "Comma-separated mock service processor sample value, format: cpuCount,sysUsage,processUsage")
  private String cvsProcessor;

  public Date getTimestamp() {
    return nonNull(timestamp0) ? new Date(timestamp0) : null;
  }
}
