package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import static java.lang.String.valueOf;

import cloud.xcan.sdf.core.angustester.domain.task.count.DataDetailBase;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewEfficiencyDetail extends ReviewEfficiencyCount implements DataDetailBase {

  private String name;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum),
        valueOf(passedReviewNum), valueOf(passedReviewRate),
        valueOf(oneTimePassedReviewNum), valueOf(oneTimePassedReviewRate),
        valueOf(twoTimePassedReviewNum), valueOf(twoTimePassedReviewRate),
        valueOf(oneTimeNotPassedReviewNum), valueOf(oneTimeNotPassedReviewRate)
    };
  }
}
