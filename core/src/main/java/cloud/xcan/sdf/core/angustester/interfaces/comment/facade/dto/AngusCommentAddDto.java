package cloud.xcan.sdf.core.angustester.interfaces.comment.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X4;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class AngusCommentAddDto {

  private Long pid;

  @NotBlank
  @ApiModelProperty(example = "Post your thoughts", required = true)
  @Length(max = DEFAULT_REMARK_LENGTH_X4)
  private String content;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"TASK", "SCRIPT", "FUNC_CASE",
      "SCENARIO"})
  @ApiModelProperty(example = "TASK", required = true)
  private CombinedTargetType targetType;

}
