package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.disk.DiskUsage.Disk;
import cloud.xcan.angus.remote.PageResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeDiskMetricsVo {

  private String deviceName;

  private PageResult<DiskValue> values;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class DiskValue {

    @JsonIgnore
    private Long timestamp0;

    /**
     * @see Disk#toString()
     */
    @Schema(description = "Comma-separated disk sample value, format: total,free,used,avail,usePercent,readsRate,writesRate,readBytesRate,writeBytesRate")
    private String cvsValue;

    public Date getTimestamp() {
      return nonNull(timestamp0) ? new Date(timestamp0) : null;
    }
  }
}
