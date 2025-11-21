package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.infra.metricsds.domain.usage.network.NetUsage.Network;
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
public class NodeNetworkMetricsVo {

  private String deviceName;

  private PageResult<NetworkValue> values;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class NetworkValue {

    @JsonIgnore
    private Long timestamp0;

    /**
     * @see Network#toString()
     */
    @Schema(description="Comma-separated network sample value, format: rxBytes,rxBytesRate,rxErrors,txBytes,txBytesRate")
    private String cvsValue;

    public Date getTimestamp() {
      return nonNull(timestamp0) ? new Date(timestamp0) : null;
    }
  }

}
