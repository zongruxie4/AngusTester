package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnplannedWorkDetail extends UnplannedWorkCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @Schema(description = "Daily processed average workload of completed tasks", hidden = true)
  protected double dailyProcessedWorkload;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(totalWorkload),
        valueOf(unplannedNum), valueOf(unplannedRate),
        valueOf(unplannedCompletedNum), valueOf(unplannedCompletedRate),
        valueOf(unplannedWorkload), valueOf(unplannedWorkload),
        valueOf(unplannedWorkloadCompleted), valueOf(unplannedWorkloadCompletedRate),
        valueOf(unplannedWorkloadProcessingTime),
    };
  }
}
