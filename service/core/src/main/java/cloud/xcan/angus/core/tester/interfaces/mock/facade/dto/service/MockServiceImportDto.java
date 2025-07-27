package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

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
public class MockServiceImportDto {

  @NotNull
  @Schema(description = "Mock service identifier for import operation", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @NotNull
  @Schema(description = "Strategy for handling duplicate APIs during import. 'COVER' overrides local APIs, 'IGNORE' skips current API.", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(example = "false", requiredMode = RequiredMode.REQUIRED,
      description = "Delete flag for non-existent APIs. If true, local APIs not present in the import will be deleted.")
  private Boolean deleteWhenNotExisted;

  @Schema(description = "API specification content in JSON or YAML format. Only one of content or file should be provided.")
  private String content;

  @Schema(type = "string", format = "binary", description = "API specification file in JSON or YAML format. For multiple files, compress into a zip before uploading.")
  private MultipartFile file;

}
