package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAssigneeWorkStatisticsDto {

  @NotNull
  @Schema(description = "Project identifier for task assignee work statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint identifier for work statistics filtering")
  private Long sprintId;

  @NotNull
  @Schema(description = "Assignee user identifier for work statistics analysis", requiredMode = RequiredMode.REQUIRED)
  private Long userId;

}
