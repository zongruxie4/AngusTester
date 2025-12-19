package cloud.xcan.angus.core.tester.interfaces.test.facade.vo;


import cloud.xcan.angus.api.enums.ReviewStatus;
import cloud.xcan.angus.core.tester.domain.test.summary.FuncCaseDetailSummary;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseReviewVo {

  private Long id;

  private String name;

  private Long planId;

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

  private String creator;

  private String avatar;

  private LocalDateTime createdDate;

}
