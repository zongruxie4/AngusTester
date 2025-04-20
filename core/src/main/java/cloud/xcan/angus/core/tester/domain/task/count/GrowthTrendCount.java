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
public class GrowthTrendCount {

  @Schema(description = "The number of requirement type tasks")
  protected long requirementNum;
  @Schema(description = "The number of story type tasks")
  protected long storyNum;
  @Schema(description = "The number of task type tasks")
  protected long taskNum;
  @Schema(description = "The number of bug type tasks")
  protected long bugNum;
  @Schema(description = "The number of api test type tasks")
  protected long apiTestNum;
  @Schema(description = "The number of scenario test type tasks")
  protected long scenarioTestNum;
  @Schema(description = "Total number of all tasks")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
