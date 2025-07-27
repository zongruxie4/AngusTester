package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class FuncTesterSummaryStatisticsDto {

  @NotNull
  @Schema(description = "Project identifier for tester summary statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Test plan identifier for statistics scope filtering")
  private Long planId;

}
