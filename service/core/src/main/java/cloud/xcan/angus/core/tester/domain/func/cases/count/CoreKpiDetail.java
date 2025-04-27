package cloud.xcan.angus.core.tester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.angus.core.tester.domain.task.count.DataDetailBase;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CoreKpiDetail extends CoreKpiCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(completedNum), valueOf(completedRate),
        valueOf(evalWorkload), valueOf(completedWorkload), valueOf(completedWorkloadRate),
        valueOf(overdueNum), valueOf(overdueRate),
        valueOf(completedOverdueNum), valueOf(completedOverdueRate),
        valueOf(reviewNum), valueOf(reviewRate),
        valueOf(completedReviewNum), valueOf(completedReviewRate),
    };
  }
}
