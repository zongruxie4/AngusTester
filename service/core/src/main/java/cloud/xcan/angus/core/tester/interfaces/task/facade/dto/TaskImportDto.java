package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TaskImportDto {

  @NotNull
  @Schema(description = "Target project identifier for task import destination", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Target sprint identifier for Agile project task assignment")
  private Long sprintId;

  @NotNull
  @Schema(description = "Duplicate handling strategy for import conflict resolution. COVER: Override existing tasks, IGNORE: Skip duplicate tasks",
      example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(type = "string", format = "binary", description = "Task import file in Excel format for bulk task creation", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile file;

}
