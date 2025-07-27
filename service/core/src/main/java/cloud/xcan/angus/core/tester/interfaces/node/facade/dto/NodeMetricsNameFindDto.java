package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class NodeMetricsNameFindDto extends PageQuery {

  @Length(max = 120)
  @Schema(description = "Device name for metrics filtering and device-specific querying")
  private String deviceName;

  @Schema(description = "Timestamp for metrics data filtering and time-based querying")
  private Long timestamp;

  @Override
  public String getDefaultOrderBy() {
    return "timestamp";
  }

  @Override
  public OrderSort getDefaultOrderSort() {
    return OrderSort.ASC;
  }
}
