package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.cases.CaseTestResult;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ResourcesFuncCount {

  @ApiModelProperty(value = "Total number of plan")
  private long allPlan;
  @ApiModelProperty(value = "The number of plans group by status")
  private LinkedHashMap<FuncPlanStatus, Integer> planByStatus = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of case")
  private long allCase;
  @ApiModelProperty(value = "The number of cases group by test result")
  private LinkedHashMap<CaseTestResult, Integer> caseByTestResult = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of cases group by review status")
  private LinkedHashMap<ReviewStatus, Integer> caseByReviewStatus = new LinkedHashMap<>();
  @ApiModelProperty(value = "The number of cases group by priority")
  private LinkedHashMap<Priority, Integer> caseByPriority = new LinkedHashMap<>();

  @ApiModelProperty(value = "Total number of review")
  private long allReview;

  @ApiModelProperty(value = "Total number of baseline")
  private long allBaseline;

}
