package cloud.xcan.sdf.core.angustester.domain.func.cases.count;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
import cloud.xcan.sdf.core.angustester.domain.task.count.AbstractOverview;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartDetail;
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
