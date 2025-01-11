package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrowthTrendCount {

  @ApiModelProperty(value = "Total number of all cases")
  protected long totalNum;

  private List<DataAssetsTimeSeries> timeSeries = new ArrayList<>();

}
