package cloud.xcan.angus.core.tester.domain.func.summary;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class FuncCaseEfficiencySummary implements IdAndCreatedDateBase<FuncCaseEfficiencySummary> {

  private Long id;

  private CaseTestResult testResult;

  private ReviewStatus reviewStatus;

  private Boolean review;

  private Priority priority;

  public LocalDateTime deadlineDate;

  private Boolean overdue;

  private Integer testNum;

  private Integer testFailNum;

  private Integer reviewNum;

  private Integer reviewFailNum;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  private Long testerId;

  private Boolean unplanned;

  private LocalDateTime testResultHandleDate;

  private Long createdBy;

  private LocalDateTime createdDate;

  public BigDecimal getEvalWorkload() {
    return nullSafe(evalWorkload, BigDecimal.ZERO);
  }

  public BigDecimal getActualWorkload() {
    return nullSafe(actualWorkload, BigDecimal.ZERO);
  }

  public BigDecimal getSavingWorkload() {
    return nullSafe(evalWorkload, BigDecimal.ZERO).subtract(
        nullSafe(actualWorkload, BigDecimal.ZERO));
  }

  public Integer getTestNum() {
    return nullSafe(testNum, 0);
  }

  public Integer getTestFailNum() {
    return nullSafe(testFailNum, 0);
  }

  public Integer getReviewNum() {
    return nullSafe(reviewNum, 0);
  }

  public Integer getReviewFailNum() {
    return nullSafe(reviewFailNum, 0);
  }
}
