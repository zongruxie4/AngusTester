package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_ARGUMENTS;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecScriptConfigReplaceDto {

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Execution name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Script type for execution specification", requiredMode = RequiredMode.REQUIRED)
  private ScriptType scriptType;

  @Valid
  @NotNull
  @Schema(description = "Execution configuration for comprehensive parameter settings", requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Size(max = MAX_ARGUMENTS)
  @Schema(description = "Plugin configuration arguments for execution customization")
  private Arguments arguments;

}
