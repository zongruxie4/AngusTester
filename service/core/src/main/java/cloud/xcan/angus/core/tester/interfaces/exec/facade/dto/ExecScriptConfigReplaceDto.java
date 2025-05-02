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
  @Schema(description = "Execution name", required = true)
  private String name;

  @NotNull
  @Schema(description = "Script type", required = true)
  private ScriptType scriptType;

  @Valid
  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Configuration configuration;

  @Size(max = MAX_ARGUMENTS)
  private Arguments arguments;

}
