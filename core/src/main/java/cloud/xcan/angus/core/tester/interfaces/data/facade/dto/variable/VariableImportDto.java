package cloud.xcan.angus.core.tester.interfaces.data.facade.dto.variable;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class VariableImportDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotNull
  @Schema(description = "Strategy for handling duplicate apis. The COVER value overrides the local api, and the IGNORE value ignores the synchronization current api.", example = "COVER", requiredMode = RequiredMode.REQUIRED)
  private StrategyWhenDuplicated strategyWhenDuplicated;

  @Schema(description = "Variable specification content. Variable definition string content in json or yaml format.")
  private String content;

  @Schema(description = "Variable specification file. Variable definition file in json or yaml format.")
  private MultipartFile file;

}
