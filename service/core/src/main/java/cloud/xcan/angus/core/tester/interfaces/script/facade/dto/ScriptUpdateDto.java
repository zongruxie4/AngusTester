package cloud.xcan.angus.core.tester.interfaces.script.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ScriptUpdateDto {

  @NotNull
  @Schema(description = "Script identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name for identification and management", example = "script-01")
  private String name;

  //@NotNull // Compatible with the preservation of legacy data.
  @Schema(description = "Type of platform for test execution.")
  @Enumerated(EnumType.STRING)
  private TestPlatform platform = TestPlatform.API;

  @Schema(description = "Script type defining the testing methodology and execution approach", example = "TEST_PERFORMANCE")
  private ScriptType type;

  //@Schema(description = "script source", example = "CREATED")
  //private ScriptSource source;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Script description for detailed information and requirements")
  private String description;

  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @Schema(description = "Script content in YAML or JSON format for execution configuration")
  private String content;

}
