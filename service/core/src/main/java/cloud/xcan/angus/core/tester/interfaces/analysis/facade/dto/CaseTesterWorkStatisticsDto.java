package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaseTesterWorkStatisticsDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Plan id")
  private Long planId;

  @NotNull
  @Schema(description = "Query work user ID", requiredMode = RequiredMode.REQUIRED)
  private Long userId;

}
