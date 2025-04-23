package cloud.xcan.angus.core.tester.interfaces.func.facade.vo;


import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncCaseDetailSummary;
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
public class FuncCaseReviewVo {

  private Long id;

  private String name;

  private Long planId;

  //@NameJoinField(id = "planId", repository = "funcPlanRepo")
  //private String planName;

  private Long reviewCaseId;

  private Long reviewId;

  private Long caseId;

  private FuncCaseDetailSummary reviewedCase;

  private Long reviewerId;

  @NameJoinField(id = "reviewerId", repository = "commonUserBaseRepo")
  private String reviewerName;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private String reviewRemark;

  private Long createdBy;

  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

}
