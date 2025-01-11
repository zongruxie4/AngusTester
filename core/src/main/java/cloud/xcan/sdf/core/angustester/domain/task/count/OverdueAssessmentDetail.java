package cloud.xcan.sdf.core.angustester.domain.task.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OverdueAssessmentDetail extends OverdueAssessmentCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        riskLevel.getMessage(),
        valueOf(totalNum), valueOf(totalWorkload),
        valueOf(overdueNum), valueOf(overdueRate),
        valueOf(overdueWorkload), valueOf(overdueWorkloadRate), valueOf(dailyProcessedWorkload),
        valueOf(overdueTime), valueOf(overdueWorkloadProcessingTime)
    };
  }
}
