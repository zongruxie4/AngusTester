package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseSummary;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
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
public class EfficiencyCaseOverview {

  private Map<Long, UserInfo> testers;

  private EfficiencyCaseCountOverview totalOverview;

  private LinkedHashMap<Priority, Integer> totalPriorityCount = new LinkedHashMap<>();

  // Used by report
  private LinkedHashMap<CaseTestResult, Integer> totalTestResultCount = new LinkedHashMap<>();

  private List<EfficiencyCaseTesterOverview> testerOverview = new ArrayList<>();

  private EfficiencyCaseRanking testerRanking;

  private Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts;

  // Unused in project report
  private List<FuncCaseSummary> cases;

}
