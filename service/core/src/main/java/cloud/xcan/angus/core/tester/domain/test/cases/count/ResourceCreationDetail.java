package cloud.xcan.angus.core.tester.domain.test.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.angus.core.tester.domain.kanban.DataAssetsTimeSeries;
import cloud.xcan.angus.core.tester.domain.issue.count.DataDetailBase;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(hidden = true)
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
