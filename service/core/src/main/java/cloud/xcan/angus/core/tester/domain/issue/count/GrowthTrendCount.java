package cloud.xcan.angus.core.tester.domain.issue.count;

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
  @Schema(description = "The number of design type tasks")
  protected long designNum;
  @Schema(description = "Total number of all tasks")
  protected long totalNum;

  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

}
