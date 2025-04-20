package cloud.xcan.angus.core.tester.interfaces.script.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class ScriptUpdateDto {

  @NotNull
  @Schema(description = "Script id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name", example = "script-01")
  private String name;

  @Schema(description = "Script type, priority is higher than the type in the script content, which controls the actual type of test execution", example = "TEST_PERFORMANCE")
  private ScriptType type;

  //@Schema(description = "script source", example = "CREATED")
  //private ScriptSource source;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Script description")
  private String description;

  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @Schema(description = "Yaml or json format script content")
  private String content;

}
