package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.sample;

import cloud.xcan.angus.remote.OrderSort;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleFindDto extends PageQuery {

  @Schema(description = "Sample completion status for state-based filtering")
  private Boolean finish;

  @Schema(description = "Sample name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Node identifier for sample filtering")
  private Long nodeId;

  @Schema(description = "Sample timestamp for temporal filtering")
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
