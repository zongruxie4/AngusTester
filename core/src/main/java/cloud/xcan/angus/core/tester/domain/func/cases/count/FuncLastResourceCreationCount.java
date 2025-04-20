package cloud.xcan.angus.core.tester.domain.func.cases.count;

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
public class FuncLastResourceCreationCount {

  @Schema(description = "Total number of plan")
  private long allPlan;
  @Schema(description = "The number of new plans added in the last one week")
  private long planByLastWeek;
  @Schema(description = "The number of new plans added in the last one month")
  private long planByLastMonth;
  @Schema(description = "The number of plans group by status")
  private LinkedHashMap<FuncPlanStatus, Integer> planByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of case")
  private long allCase;
  @Schema(description = "The number of cases overdue")
  private long caseByOverdue;
  @Schema(description = "The number of new cases added in the last one week")
  private long caseByLastWeek;
  @Schema(description = "The number of new cases added in the last one month")
  private long caseByLastMonth;
  @Schema(description = "The number of cases group by test result")
  private LinkedHashMap<CaseTestResult, Integer> caseByTestResult = new LinkedHashMap<>();
  @Schema(description = "The number of cases group by review status")
  private LinkedHashMap<ReviewStatus, Integer> caseByReviewStatus = new LinkedHashMap<>();
  @Schema(description = "The number of cases group by priority")
  private LinkedHashMap<Priority, Integer> caseByPriority = new LinkedHashMap<>();

  @Schema(description = "Total number of review")
  private long allReview;
  @Schema(description = "The number of new reviews added in the last one week")
  private long reviewByLastWeek;
  @Schema(description = "The number of new reviews added in the last one month")
  private long reviewByLastMonth;
  @Schema(description = "The number of reviews group by status")
  private LinkedHashMap<FuncPlanStatus, Integer> reviewByStatus = new LinkedHashMap<>();

  @Schema(description = "Total number of baselines")
  private long allBaseline;
  @Schema(description = "The number of new baselines added in the last one week")
  private long baselineByLastWeek;
  @Schema(description = "The number of new baselines added in the last one month")
  private long baselineByLastMonth;

}
