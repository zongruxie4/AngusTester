package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BackloggedDetail extends BackloggedCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @Schema(description = "The completion time for all processed tasks", hidden = true)
  protected long processedInDay;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(totalWorkload),
        valueOf(dailyProcessedNum), valueOf(dailyProcessedWorkload),
        valueOf(backloggedNum), valueOf(backloggedRate),
        valueOf(backloggedWorkload), valueOf(backloggedWorkloadRate),
        valueOf(backloggedCompletionTime)
    };
  }
}
