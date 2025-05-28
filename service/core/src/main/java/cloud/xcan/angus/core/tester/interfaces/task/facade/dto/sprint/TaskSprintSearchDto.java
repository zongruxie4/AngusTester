package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.sprint;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class TaskSprintSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private String name;

  private TaskSprintStatus status;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Long ownerId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
