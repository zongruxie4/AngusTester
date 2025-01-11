package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.count.BurnDownChartCount;
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
