package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.issue.count.FailureAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.issue.summary.TaskSummary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CtoTaskAssigneeOverview extends CtoTaskOverview{

  private Long assigneeId;

  private TotalProgressCount totalProgressOverview = new TotalProgressCount();

  private BackloggedCount backloggedTaskCount = new BackloggedCount();

  private OverdueAssessmentCount overdueAssessmentCount = new OverdueAssessmentCount();

  private Map<String, RecentDeliveryCount> recentDeliveryCount = new HashMap<>();

  private UnplannedWorkCountBase unplannedWorkCount = new UnplannedWorkCount();

  private FailureAssessmentCount failureAssessmentCount = new FailureAssessmentCount();

  private LeadTimeCountBase leadTimeCount = new LeadTimeCount();

  private LinkedHashMap<TaskStatus, Integer> totalStatusCount = new LinkedHashMap<>();

  private LinkedHashMap<TaskType, Integer> totalTypeCount = new LinkedHashMap<>();

  @JsonIgnore
  @Schema(hidden = true)
  private final int memberNum = 0;
  @JsonIgnore
  @Schema(hidden = true)
  private final int assigneeNum = 0;
  @JsonIgnore
  @Schema(hidden = true)
  private final Map<Long, UserInfo> assignees = null; // Actually are members
  @JsonIgnore
  @Schema(hidden = true)
  private final List<CtoTaskAssigneeOverview> assigneeOverview = null;
  @JsonIgnore
  @Schema(hidden = true)
  // Unused in project report
  private final List<TaskSummary> tasks = null;
}
