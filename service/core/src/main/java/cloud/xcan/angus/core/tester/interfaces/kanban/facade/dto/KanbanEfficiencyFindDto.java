package cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
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
public class KanbanEfficiencyFindDto extends PageQuery {

  @Schema(description = "Query organization type, default USER")
  private AuthObjectType creatorObjectType;

  @Schema(description = "Query organization id")
  private Long creatorObjectId;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Case plan or task sprint id")
  private Long planId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resources creation end date")
  private LocalDateTime createdDateEnd;

}
