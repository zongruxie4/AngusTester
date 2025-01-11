package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseSummary;
import cloud.xcan.sdf.core.angustester.domain.task.count.BackloggedCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.LeadTimeCountBase;
import cloud.xcan.sdf.core.angustester.domain.task.count.OverdueAssessmentCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.RecentDeliveryCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCount;
import cloud.xcan.sdf.core.angustester.domain.task.count.UnplannedWorkCountBase;
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
public class CtoCaseTesterOverview extends CtoCaseOverview {

  private long testerId;

  private TotalProgressCount totalProgressOverview = new TotalProgressCount();

  private BackloggedCount backloggedCount = new BackloggedCount();

  private OverdueAssessmentCount overdueAssessmentCount = new OverdueAssessmentCount();

  private Map<String, RecentDeliveryCount> recentDeliveryCount = new HashMap<>();

  private UnplannedWorkCountBase unplannedWorkCount = new UnplannedWorkCount();

  private TestApisCount apisTestCount = new TestApisCount();

  private TestScenarioCount scenarioTestCount = new TestScenarioCount();

  private LeadTimeCountBase leadTimeCount = new LeadTimeCount();

  private LinkedHashMap<CaseTestResult, Integer> totalTestResultCount = new LinkedHashMap<>();

  private LinkedHashMap<ReviewStatus, Integer> totalReviewStatusCount = new LinkedHashMap<>();

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final int memberNum = 0;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final int testerNum = 0;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final Map<Long, UserInfo> testers = null; // Actually are members
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final List<CtoCaseTesterOverview> testerOverview = null;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  // Unused in project report
  private final List<FuncCaseSummary> cases = null;

}
