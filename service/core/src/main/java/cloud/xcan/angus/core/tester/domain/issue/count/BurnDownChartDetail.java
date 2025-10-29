package cloud.xcan.angus.core.tester.domain.issue.count;

import static java.lang.String.valueOf;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BurnDownChartDetail implements DataDetailBase {

  private String name;

  private long totalNum;

  private long completedNum;

  private long remainedNum;

  private double totalWorkload;

  private double completedWorkload;

  private double remainedWorkload;

  @Override
  public String[] toValues() {
    return new String[]{
        name,
        valueOf(totalNum), valueOf(completedNum),
        valueOf(remainedNum), valueOf(totalWorkload),
        valueOf(completedWorkload), valueOf(remainedWorkload)
    };
  }
}
