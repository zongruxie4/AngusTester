package cloud.xcan.angus.core.tester.interfaces.comment.facade.dto;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class AngusCommentFindDto {

  @NotNull
  @Schema(description = "Target resource identifier for comment tree query", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"TASK", "SCRIPT", "FUNC_CASE", "SCENARIO"})
  @Schema(description = "Target resource type for comment tree filtering", example = "TASK", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

}



