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
public class TaskCreatorStatisticsDto {

  @NotNull
  @Schema(description = "Project identifier for task creator statistics analysis", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint identifier for statistics scope filtering")
  private Long sprintId;

  @Schema(description = "Creator object type for analysis grouping, defaults to USER", example = "USER")
  private AuthObjectType creatorObjectType;

  @Schema(description = "Creator object identifier, defaults to current user identifier")
  private Long creatorObjectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resource creation period start timestamp")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resource creation period end timestamp")
  private LocalDateTime createdDateEnd;

}
