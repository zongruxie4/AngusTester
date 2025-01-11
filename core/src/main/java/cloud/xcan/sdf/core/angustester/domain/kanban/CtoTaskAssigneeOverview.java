package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.FailureAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.summary.TaskSummary;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
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
  @ApiModelProperty(hidden = true)
  private final int memberNum = 0;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final int assigneeNum = 0;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Map<Long, UserInfo> assignees = null; // Actually are members
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final List<CtoTaskAssigneeOverview> assigneeOverview = null;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  // Unused in project report
  private final List<TaskSummary> tasks = null;
}
