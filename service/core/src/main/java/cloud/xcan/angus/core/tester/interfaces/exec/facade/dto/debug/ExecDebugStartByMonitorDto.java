package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug;

import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecDebugStartByMonitorDto {

  @Schema(description = "Broadcast flag for multi-controller notification and handling", requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @Schema(description = "Debug session identifier for session management")
  private Long id;

  @NotNull
  @Schema(description = "Monitor identifier for debug execution reference", requiredMode = RequiredMode.REQUIRED)
  private Long monitorId;

  @NotNull
  @Schema(description = "Scenario identifier for debug execution reference", requiredMode = RequiredMode.REQUIRED)
  private Long scenarioId;

  @NotNull
  @Schema(description = "Script identifier for debug execution reference", requiredMode = RequiredMode.REQUIRED)
  private Long scriptId;

  @Schema(description = "Actual script type for debug execution specification")
  private ScriptType scriptType;

  @Valid
  @Schema(description = "Common configuration parameters for debug script execution")
  private Configuration configuration;

  @Schema(description = "Plugin configuration parameters for debug execution customization")
  private Arguments arguments = new Arguments();

  @Schema(description = "HTTP server configuration for debug environment setup")
  private List<Server> serverSetting;

}
