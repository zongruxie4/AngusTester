package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.dataset;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Accessors(chain = true)
public class DatasetImportDto{

  @NotNull
  @Schema(description = "Project identifier for dataset import destination", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotNull
  @Schema(description = "Duplicate handling strategy for import conflict resolution. COVER overrides existing datasets, IGNORE skips duplicates", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @Schema(description = "Dataset specification content in JSON or YAML format for direct import")
  private String content;

  @Schema(description = "Dataset specification file in JSON or YAML format for file-based import", type = "string", format = "binary")
  private MultipartFile file;

}
