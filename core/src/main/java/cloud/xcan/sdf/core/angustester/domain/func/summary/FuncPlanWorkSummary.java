package cloud.xcan.sdf.core.angustester.domain.func.summary;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.cases.count.FuncCaseCount;
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
