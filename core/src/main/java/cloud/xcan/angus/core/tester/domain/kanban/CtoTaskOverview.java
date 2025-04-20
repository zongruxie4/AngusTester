package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.task.count.FailureAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.task.summary.TaskSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CtoTaskOverview {

  private int memberNum;
  private int assigneeNum;

  private Map<Long, UserInfo> assignees; // Actually are members

  private TotalProgressCount totalProgressOverview = new TotalProgressCount();

  private BackloggedCount backloggedCount = new BackloggedCount();

  private OverdueAssessmentCount overdueAssessmentCount = new OverdueAssessmentCount();

  private Map<String, RecentDeliveryCount> recentDeliveryCount = new HashMap<>();

  private UnplannedWorkCountBase unplannedWorkCount = new UnplannedWorkCount();

  private FailureAssessmentCount failureAssessmentCount = new FailureAssessmentCount();

  private LeadTimeCountBase leadTimeCount = new LeadTimeCount();

  private LinkedHashMap<TaskStatus, Integer> totalStatusCount = new LinkedHashMap<>();

  private LinkedHashMap<TaskType, Integer> totalTypeCount = new LinkedHashMap<>();

  private List<CtoTaskAssigneeOverview> assigneeOverview = new ArrayList<>();

  // Unused in project report
  private List<TaskSummary> tasks;

}
