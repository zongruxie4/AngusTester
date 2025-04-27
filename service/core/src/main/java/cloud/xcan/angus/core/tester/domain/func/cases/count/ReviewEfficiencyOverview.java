package cloud.xcan.angus.core.tester.domain.func.cases.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.count.AbstractOverview;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewEfficiencyOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers = new HashMap<>();

  private ReviewEfficiencyCount totalOverview = new ReviewEfficiencyCount();

  private Map<Long, ReviewEfficiencyCount> testersOverview = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<ReviewEfficiencyDetail> dataDetails = new ArrayList<>();

}
