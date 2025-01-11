package cloud.xcan.sdf.core.angustester.domain.func.summary;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncCaseCount;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.sdf.core.angustester.domain.project.summary.ProjectSummary;
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
