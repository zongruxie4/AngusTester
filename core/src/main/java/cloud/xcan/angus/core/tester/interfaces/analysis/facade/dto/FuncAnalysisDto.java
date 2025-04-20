package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class FuncAnalysisDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Plan id")
  private Long planId;

  @Schema(description = "Analysis organization type")
  private AuthObjectType orgType;
  @Schema(description = "Analysis organization id")
  private Long orgId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Analysis data start time")
  private LocalDateTime startTime;
  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Analysis data end time")
  private LocalDateTime endTime;

  @Schema(description = "Contains tester analysis, default true")
  private boolean containsUserAnalysis = true;
  @Schema(description = "Contains data details, default true")
  private boolean containsDataDetail = true;

}
