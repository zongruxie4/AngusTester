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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CtoCaseOverview {

  private int memberNum;
  private int testerNum;

  private Map<Long, UserInfo> testers; // Actually are members

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

  private List<CtoCaseTesterOverview> testerOverview = new ArrayList<>();

  // Unused in project report
  private List<FuncCaseSummary> cases;

}
