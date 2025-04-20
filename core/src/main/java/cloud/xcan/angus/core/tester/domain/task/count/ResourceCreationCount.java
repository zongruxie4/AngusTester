package cloud.xcan.angus.core.tester.domain.task.count;

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

  @Schema(description = "The number of backlogs")
  protected long backlogNum;
  @Schema(description = "The number of sprints")
  protected long sprintNum;
  @Schema(description = "The number of tasks")
  protected long taskNum;
  @Schema(description = "The number of meeting")
  protected long meetingNum;
  @Schema(description = "Total number of all resources")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
