package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.task.count.DataDetailBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrowthTrendDetail extends GrowthTrendCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private List<DataAssetsTimeSeries> timeSeries = new ArrayList<>();

  private List<Integer> timeSeriesDetail = new ArrayList<>();

  @Override
  public String[] toValues() {
    List<String> values = new ArrayList<>();
    values.add(name);
    values.add(valueOf(totalNum));
    values.addAll(timeSeriesDetail.stream().map(String::valueOf).collect(Collectors.toList()));
    return values.toArray(new String[0]);
  }
}
