package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.issue.count.BackloggedCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCount;
import cloud.xcan.angus.core.tester.domain.issue.count.LeadTimeCountBase;
import cloud.xcan.angus.core.tester.domain.issue.count.OverdueAssessmentCount;
import cloud.xcan.angus.core.tester.domain.issue.count.RecentDeliveryCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCount;
import cloud.xcan.angus.core.tester.domain.issue.count.UnplannedWorkCountBase;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseSummary;
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
  @Schema(hidden = true)
  private final int memberNum = 0;
  @JsonIgnore
  @Schema(hidden = true)
  private final int testerNum = 0;
  @JsonIgnore
  @Schema(hidden = true)
  private final Map<Long, UserInfo> testers = null; // Actually are members
  @JsonIgnore
  @Schema(hidden = true)
  private final List<CtoCaseTesterOverview> testerOverview = null;
  @JsonIgnore
  @Schema(hidden = true)
  // Unused in project report
  private final List<FuncCaseSummary> cases = null;

}
