package cloud.xcan.angus.core.tester.domain.func.cases.count;

import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResourceCreationCount {

  @Schema(description = "The number of plans")
  protected long planNum;
  @Schema(description = "The number of cases")
  protected long caseNum;
  @Schema(description = "The number of reviews")
  protected long reviewNum;
  @Schema(description = "The number of baselines")
  protected long baselineNum;
  @Schema(description = "The number of analysis")
  protected long analysisNum;
  @Schema(description = "Total number of all resources")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
