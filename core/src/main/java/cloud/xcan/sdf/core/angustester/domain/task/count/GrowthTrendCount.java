package cloud.xcan.sdf.core.angustester.domain.task.count;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrowthTrendCount {

  @ApiModelProperty(value = "The number of requirement type tasks")
  protected long requirementNum;
  @ApiModelProperty(value = "The number of story type tasks")
  protected long storyNum;
  @ApiModelProperty(value = "The number of task type tasks")
  protected long taskNum;
  @ApiModelProperty(value = "The number of bug type tasks")
  protected long bugNum;
  @ApiModelProperty(value = "The number of api test type tasks")
  protected long apiTestNum;
  @ApiModelProperty(value = "The number of scenario test type tasks")
  protected long scenarioTestNum;
  @ApiModelProperty(value = "Total number of all tasks")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
