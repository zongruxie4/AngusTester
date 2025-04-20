package cloud.xcan.angus.core.tester.domain.kanban;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResourcesFuncCount {

  @Schema(description = "Total number of plan")
  private long allPlan;
  @Schema(description = "The number of plans group by status")
  private LinkedHashMap<FuncPlanStatus, Integer> planByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of case")
  private long allCase;
  @Schema(description = "The number of cases group by test result")
  private LinkedHashMap<CaseTestResult, Integer> caseByTestResult = new LinkedHashMap<>();
  @Schema(description = "The number of cases group by review status")
  private LinkedHashMap<ReviewStatus, Integer> caseByReviewStatus = new LinkedHashMap<>();
  @Schema(description = "The number of cases group by priority")
  private LinkedHashMap<Priority, Integer> caseByPriority = new LinkedHashMap<>();

  @Schema(description = "Total number of review")
  private long allReview;

  @Schema(description = "Total number of baseline")
  private long allBaseline;

}
