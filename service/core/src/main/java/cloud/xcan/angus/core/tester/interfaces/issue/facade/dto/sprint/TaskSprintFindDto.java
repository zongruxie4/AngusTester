package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.sprint;

import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintFindDto extends PageQuery {

  @Schema(description = "Sprint identifier for specific sprint lookup")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for sprint filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint display name for partial matching search")
  private String name;

  @Schema(description = "Sprint execution status for workflow filtering")
  private TaskSprintStatus status;

  @Schema(description = "Sprint execution start date for timeline filtering")
  private LocalDateTime startDate;

  @Schema(description = "Sprint completion deadline for timeline filtering")
  private LocalDateTime deadlineDate;

  @Schema(description = "Sprint owner identifier for responsibility filtering")
  private Long ownerId;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}



