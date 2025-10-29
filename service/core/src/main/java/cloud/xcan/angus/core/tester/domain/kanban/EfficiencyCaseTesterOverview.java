package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.task.count.BurnDownChartCount;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class EfficiencyCaseTesterOverview {

  private Long testerId;

  private EfficiencyCaseCountOverview statusOverview;

  private LinkedHashMap<Priority, Integer> priorityOverview = new LinkedHashMap<>();

  // Used by report
  private LinkedHashMap<CaseTestResult, Integer> testResultOverview = new LinkedHashMap<>();

  // Used by report
  private Map<BurnDownResourceType, BurnDownChartCount> burnDownCharts;

}
