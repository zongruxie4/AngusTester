package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

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
public class MockServiceImportDto {

  @NotNull
  @Schema(description = "Import mock service id.", requiredMode = RequiredMode.REQUIRED)
  private Long mockServiceId;

  @NotNull
  @Schema(description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @NotNull
  @Schema(example = "false", requiredMode = RequiredMode.REQUIRED,
      description = "The delete flag when not existed. Whether to delete a local api when it does not exist in the current synchronized data.")
  private Boolean deleteWhenNotExisted;

  @Schema(description = "Apis specification content. API definition string content in json or yaml format, multiple files import is not supported")
  private String content;

  @Schema(description = "Apis specification file. API definition file in json or yaml format, multiple files need to be compressed into a zip file before uploading")
  private MultipartFile file;

}
