package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.sdf.core.angustester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.sdf.core.angustester.domain.task.count.DataDetailBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResourceCreationDetail extends ResourceCreationCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private Map<String, List<DataAssetsTimeSeries>> timeSeries = new HashMap<>();

  private List<String> timeSeriesDetail = new ArrayList<>();

  @Override
  public String[] toValues() {
    List<String> values = new ArrayList<>();
    values.add(name);
    values.add(valueOf(planNum));
    values.add(valueOf(caseNum));
    values.add(valueOf(reviewNum));
    values.add(valueOf(baselineNum));
    values.add(valueOf(analysisNum));
    values.add(valueOf(totalNum));
    values.addAll(timeSeriesDetail);
    return values.toArray(new String[0]);
  }
}
