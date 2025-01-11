package cloud.xcan.sdf.core.angustester.domain.task.count;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.kanban.BurnDownResourceType;
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

  private Map<Long, UserInfo> assignees;

  private Map<BurnDownResourceType, BurnDownChartCount> totalBurnDownCharts = new HashMap<>();

  private Map<Long, Map<BurnDownResourceType, BurnDownChartCount>> assigneesBurnDownCharts = new LinkedHashMap<>();

  private String[] dataDetailTitles = new String[]{};
  private List<BurnDownChartDetail> dataDetails = new ArrayList<>();

}
