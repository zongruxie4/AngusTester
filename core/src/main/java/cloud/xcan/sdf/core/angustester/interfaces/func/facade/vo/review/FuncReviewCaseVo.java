package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseListVo;
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
public class FuncReviewCaseVo {

  private Long id;

  private String name;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private Long planId;

  @NameJoinField(id = "planId", repository = "funcPlanRepo")
  private String planName;

  private Long reviewId;

  private Long caseId;

  private FuncCaseListVo caseInfo;

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
