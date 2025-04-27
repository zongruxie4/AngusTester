package cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review;


import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseDetailVo;
import cloud.xcan.angus.remote.NameJoinField;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCaseDetailVo {

  private Long id;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Long reviewId;

  @NameJoinField(id = "reviewId", repository = "funcReviewRepo")
  private String reviewName;

  private Long caseId;

  private FuncCaseDetailSummary reviewedCase;

  private FuncCaseDetailVo latestCase;

  private Long reviewerId;

  @NameJoinField(id = "reviewerId", repository = "commonUserBaseRepo")
  private String reviewerName;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

}
