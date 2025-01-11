package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X2;

import cloud.xcan.sdf.api.enums.ReviewStatus;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseReviewDto {

  @NotNull
  @ApiModelProperty(value = "Functional testing case id", required = true)
  private Long id;

  @NotNull
  @EnumPart(enumClass = ReviewStatus.class, allowableValues = {"PASSED", "FAILED"})
  @ApiModelProperty(value = "The result of case review", required = true)
  private ReviewStatus reviewStatus;

  @Length(max = DEFAULT_DESC_LENGTH_X2)
  @ApiModelProperty(value = "The remark of case review")
  private String reviewRemark;

}
