package cloud.xcan.angus.core.tester.domain.test.cases.count;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.kanban.BurnDownResourceType;
import cloud.xcan.angus.core.tester.domain.issue.count.AbstractOverview;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartDetail;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BurnDownChartOverview extends AbstractOverview {

  private Map<Long, UserInfo> testers;

  private Map<BurnDownResourceType, BurnDownChartCount> totalBurnDownCharts = new HashMap<>();

  private Map<Long, Map<BurnDownResourceType, BurnDownChartCount>> testersBurnDownCharts = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<BurnDownChartDetail> dataDetails = new ArrayList<>();

}
