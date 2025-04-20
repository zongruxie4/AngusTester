package cloud.xcan.angus.core.tester.domain.func.summary;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.cases.count.FuncCaseCount;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class FuncPlanWorkSummary {

  private FuncPlanSummary summary;

  private FuncCaseCount caseCount;

  private Progress progress;

  private List<UserInfo> members;

  private List<FuncTesterWorkSummary> testerSummaries = new ArrayList<>();

  private LinkedHashMap<CaseTestResult, Integer> caseByTestResult;

  private LinkedHashMap<ReviewStatus, Integer> caseByReviewStatus;

  private LinkedHashMap<Priority, Integer> caseByPriority;

}
