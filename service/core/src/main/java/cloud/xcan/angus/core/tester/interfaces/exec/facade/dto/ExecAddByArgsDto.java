package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecAddByArgsDto {

  @NotNull
  @Schema(description = "Project identifier for execution association", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Execution name for identification and organization")
  private String name;

  @NotNull
  @Schema(description = "Script type for execution specification", requiredMode = RequiredMode.REQUIRED)
  private ScriptType scriptType;

  @NotEmpty
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Execution plugin name with automatic version selection for highest compatibility", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Valid
  @NotNull
  @Schema(description = "Execution configuration for comprehensive parameter settings", requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Valid
  @NotNull
  @Schema(description = "Execution task information for pipeline definition", requiredMode = RequiredMode.REQUIRED)
  private Task task;

  @Schema(description = "Trial execution flag for testing and validation purposes")
  private Boolean trial;
}
