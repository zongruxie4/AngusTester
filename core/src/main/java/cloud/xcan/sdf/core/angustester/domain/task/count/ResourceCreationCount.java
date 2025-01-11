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
public class ResourceCreationCount {

  @ApiModelProperty(value = "The number of backlogs")
  protected long backlogNum;
  @ApiModelProperty(value = "The number of sprints")
  protected long sprintNum;
  @ApiModelProperty(value = "The number of tasks")
  protected long taskNum;
  @ApiModelProperty(value = "The number of meeting")
  protected long meetingNum;
  @ApiModelProperty(value = "The number of analysis")
  protected long analysisNum;
  @ApiModelProperty(value = "Total number of all resources")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
