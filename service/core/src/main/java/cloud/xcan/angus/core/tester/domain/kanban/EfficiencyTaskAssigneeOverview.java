package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.count.BurnDownChartCount;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class EfficiencyTaskAssigneeOverview {

  private Long assigneeId;

  private EfficiencyTaskCountOverview totalOverview = new EfficiencyTaskCountOverview();

  // Used by report
  private LinkedHashMap<TaskStatus, Integer> statusOverview = new LinkedHashMap<>();

  private LinkedHashMap<TaskType, Integer> typeOverview = new LinkedHashMap<>();

  private LinkedHashMap<Priority, Integer> priorityOverview = new LinkedHashMap<>();

  // Used by report
  private Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts;

}
