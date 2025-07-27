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
public class TaskAnalysisDto {

  @NotNull
  @Schema(description = "Project identifier for task analysis", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint identifier for analysis scope filtering")
  private Long sprintId;

  @Schema(description = "Organization type for analysis grouping")
  private AuthObjectType orgType;
  @Schema(description = "Organization identifier for analysis filtering")
  private Long orgId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Analysis data collection start timestamp")
  private LocalDateTime startTime;
  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Analysis data collection end timestamp")
  private LocalDateTime endTime;

  @Schema(description = "Include assignee analysis in results, defaults to true")
  private boolean containsUserAnalysis = true;
  @Schema(description = "Include detailed data in results, defaults to true")
  private boolean containsDataDetail = true;

}
