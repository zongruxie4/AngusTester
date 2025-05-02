package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug;

import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecDebugStartByScriptDto {

  @Schema(description = "Whether to notify other controllers to handle", requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @Schema(description = "Execution id")
  private Long id;

  @NotNull
  @Schema(description = "Execution script id", requiredMode = RequiredMode.REQUIRED)
  private Long scriptId;

  @Schema(description = "Execute actual script type")
  private ScriptType scriptType;

  @Valid
  @Schema(description = "Common configuration parameters for script execution")
  private Configuration configuration;

  @Schema(description="Plugin configuration parameters")
  private Arguments arguments = new Arguments();

}
