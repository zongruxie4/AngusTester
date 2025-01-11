package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncCaseDetailSummary;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
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
