package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.ReviewStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncReviewCaseFindDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Review id", required = true)
  private Long reviewId;

  private Long planId;

  private Long caseId;

  private String caseName;

  private String caseCode;

  private Long reviewerId;

  private LocalDateTime reviewDate;

  private ReviewStatus reviewStatus;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



