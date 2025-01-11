package cloud.xcan.sdf.core.angustester.interfaces.comment.facade.dto;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class AngusCommentFindDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"TASK", "SCRIPT", "FUNC_CASE",
      "SCENARIO"})
  @ApiModelProperty(example = "TASK", required = true)
  private CombinedTargetType targetType;

}



