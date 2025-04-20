package cloud.xcan.angus.core.tester.domain.task.count;

import static java.lang.String.valueOf;

import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BugDetail extends BugCount implements DataDetailBase {

  private String name;

  @JsonIgnore
  @Schema(description = "The number of bugs by different levels", hidden = true)
  private LinkedHashMap<BugLevel, Integer> bugLevelCount = new LinkedHashMap<>();
  @JsonIgnore
  @Schema(description = "The rate of bugs by different levels", hidden = true)
  private LinkedHashMap<BugLevel, Double> bugLevelRate = new LinkedHashMap<>();

  /**
   * The number and rate of failures by different levels.
   */
  private long criticalNum;
  private double criticalRate;
  private long majorNum;
  private double majorRate;
  private long minorNum;
  private double minorRate;
  private long trivialNum;
  private double trivialRate;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(totalWorkload),
        valueOf(bugNum), valueOf(bugRate),
        valueOf(validBugNum), valueOf(validBugRate),
        valueOf(invalidBugNum), valueOf(invalidBugRate),
        valueOf(bugWorkload), valueOf(bugWorkloadRate),
        valueOf(missingBugNum), valueOf(missingBugRate),
        valueOf(totalPassedBugNum),
        valueOf(oneTimePassedBugNum), valueOf(oneTimePassedBugRate),
        valueOf(criticalNum), valueOf(criticalRate),
        valueOf(majorNum), valueOf(majorRate),
        valueOf(minorNum), valueOf(minorRate),
        valueOf(trivialNum), valueOf(trivialRate)
    };
  }

}
