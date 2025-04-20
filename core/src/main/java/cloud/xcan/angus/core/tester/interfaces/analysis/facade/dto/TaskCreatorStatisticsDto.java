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
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private Long sprintId;

  @Schema(description = "Creator object type, Default USER", example = "USER")
  private AuthObjectType creatorObjectType;

  @Schema(description = "Creator object id, Default current user id")
  private Long creatorObjectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resources creation start date")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Resources creation end date")
  private LocalDateTime createdDateEnd;

}
