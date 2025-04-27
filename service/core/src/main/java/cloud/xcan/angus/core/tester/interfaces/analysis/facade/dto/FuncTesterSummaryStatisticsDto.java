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
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long planId;

}
