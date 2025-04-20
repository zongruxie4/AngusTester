package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class EfficiencyTaskOverview {

  private Map<Long, UserInfo> assignees;

  private EfficiencyTaskCountOverview totalOverview;

  // Used by report
  private LinkedHashMap<TaskStatus, Integer> totalStatusCount = new LinkedHashMap<>();

  private LinkedHashMap<TaskType, Integer> totalTypeCount = new LinkedHashMap<>();

  private LinkedHashMap<Priority, Integer> totalPriorityCount = new LinkedHashMap<>();

  private EfficiencyTaskRanking assigneeRanking;

  private Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts;

  private List<EfficiencyTaskAssigneeOverview> assigneeOverview = new ArrayList<>();

  // Unused in project report
  private List<TaskSummary> tasks;
}
