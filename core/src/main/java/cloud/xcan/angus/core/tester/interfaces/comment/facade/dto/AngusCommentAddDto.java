package cloud.xcan.angus.core.tester.interfaces.comment.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REMARK_LENGTH_X4;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class AngusCommentAddDto {

  private Long pid;

  @NotBlank
  @Schema(example = "Post your thoughts", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_REMARK_LENGTH_X4)
  private String content;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"TASK", "SCRIPT", "FUNC_CASE",
      "SCENARIO"})
  @Schema(example = "TASK", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

}
