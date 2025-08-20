package cloud.xcan.angus.core.tester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.task.count.DataDetailBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GrowthTrendDetail extends GrowthTrendCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @Schema(hidden = true)
  private List<DataAssetsTimeSeries> timeSeries = new ArrayList<>();

  private List<Integer> timeSeriesDetail = new ArrayList<>();

  @Override
  public String[] toValues() {
    List<String> values = new ArrayList<>();
    values.add(name);
    values.add(valueOf(totalNum));
    values.addAll(timeSeriesDetail.stream().map(String::valueOf).toList());
    return values.toArray(new String[0]);
  }
}
