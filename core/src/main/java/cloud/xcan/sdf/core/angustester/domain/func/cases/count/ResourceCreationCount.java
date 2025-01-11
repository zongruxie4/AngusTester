package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResourceCreationCount {

  @ApiModelProperty(value = "The number of plans")
  protected long planNum;
  @ApiModelProperty(value = "The number of cases")
  protected long caseNum;
  @ApiModelProperty(value = "The number of reviews")
  protected long reviewNum;
  @ApiModelProperty(value = "The number of baselines")
  protected long baselineNum;
  @ApiModelProperty(value = "The number of analysis")
  protected long analysisNum;
  @ApiModelProperty(value = "Total number of all resources")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
