package cloud.xcan.angus.core.tester.domain.test.cases.count;

import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrowthTrendCount {

  @Schema(description = "Total number of all cases")
  protected long totalNum;

  private List<DataAssetsTimeSeries> timeSeries = new ArrayList<>();

}
