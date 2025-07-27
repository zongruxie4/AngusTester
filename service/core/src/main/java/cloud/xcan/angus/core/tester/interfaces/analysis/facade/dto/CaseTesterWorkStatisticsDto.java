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
  @Schema(description = "Project identifier for case tester work statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Test plan identifier for filtering case statistics")
  private Long planId;

  @NotNull
  @Schema(description = "Tester user identifier for work statistics analysis", requiredMode = RequiredMode.REQUIRED)
  private Long userId;

}
