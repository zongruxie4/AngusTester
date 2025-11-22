package cloud.xcan.angus.core.tester.domain.setting;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ContentFilterSetting {

  @NotNull
  @Schema(description = "Report resource or activity type", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @NotNull
  @Schema(description = "Report resource id", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @Schema(description = "Query organization type, default USER")
  private AuthObjectType creatorObjectType;

  @Schema(description = "Query organization id")
  private Long creatorObjectId;

  // Specify during report creation
  // @NotNull
  // @Schema(description = "Project id", required = true)
  // private Long projectId;

  @Schema(description = "Case plan or task sprint id. Just for the front-end to display the parent of the target.")
  private Long planOrSprintId;

  @Schema(description = "Test evaluation purposes")
  private List<EvaluationPurpose> evaluationPurposes;

  @Schema(description = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @Schema(description = "Resources creation end date")
  private LocalDateTime createdDateEnd;

}
