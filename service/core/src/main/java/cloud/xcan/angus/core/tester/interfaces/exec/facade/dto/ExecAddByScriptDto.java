package cloud.xcan.angus.core.tester.interfaces.exec.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

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
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class ExecAddByScriptDto {

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Execution name, when empty, the script name will be used")
  private String name;

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

  @Schema(description = "Trial execution test flag")
  private Boolean trial;

}
