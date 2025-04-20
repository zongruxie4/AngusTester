package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */@Valid

@Getter
@Setter
@Accessors(chain = true)
public class TaskImportDto {

  @NotNull
  @Schema(description = "Import project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Import sprint id, it is required for agile project management")
  private Long sprintId;

  @NotNull
  @Schema(description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.",
      example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(description = "Import task file, only support excel file.", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile file;

}
