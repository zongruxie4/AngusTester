package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LeadTimeDetail extends LeadTimeCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalProcessingTime), valueOf(userAvgProcessingTime),
        valueOf(minProcessingTime), valueOf(maxProcessingTime),
        valueOf(p50ProcessingTime), valueOf(p75ProcessingTime), valueOf(p90ProcessingTime),
        valueOf(p95ProcessingTime), valueOf(p99ProcessingTime)
    };
  }
}
