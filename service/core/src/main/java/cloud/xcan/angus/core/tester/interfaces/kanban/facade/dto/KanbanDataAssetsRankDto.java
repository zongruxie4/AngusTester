package cloud.xcan.angus.core.tester.interfaces.kanban.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
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
public class KanbanDataAssetsRankDto {

  @Schema(description = "Creator organization type for data assets ranking filtering with USER as default")
  private AuthObjectType creatorObjectType;

  @Schema(description = "Creator organization identifier for data assets ranking filtering")
  private Long creatorObjectId;

  @NotNull
  @Schema(description = "Project identifier for data assets ranking scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Data assets creation start date for ranking temporal filtering")
  private LocalDateTime createdDateStart;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Data assets creation end date for ranking temporal filtering")
  private LocalDateTime createdDateEnd;

}
