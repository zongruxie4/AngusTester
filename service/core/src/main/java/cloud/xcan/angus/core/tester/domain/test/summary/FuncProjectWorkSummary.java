package cloud.xcan.angus.core.tester.domain.test.summary;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.test.cases.count.FuncCaseCount;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.project.summary.ProjectSummary;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncProjectWorkSummary {

  private ProjectSummary summary;

  private FuncCaseCount caseCount = new FuncCaseCount();

  private LinkedHashMap<FuncPlanStatus, Integer> planByStatus = new LinkedHashMap<>();

  private LinkedHashMap<CaseTestResult, Integer> caseByTestResult = new LinkedHashMap<>();

  private LinkedHashMap<ReviewStatus, Integer> caseByReviewStatus = new LinkedHashMap<>();

  private LinkedHashMap<Priority, Integer> caseByPriority = new LinkedHashMap<>();

  private List<FuncPlanWorkSummary> planSummaries = new ArrayList<>();

}
