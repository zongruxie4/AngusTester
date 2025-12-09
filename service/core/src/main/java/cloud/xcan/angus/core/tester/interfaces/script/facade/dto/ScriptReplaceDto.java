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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ScriptReplaceDto {

  @Schema(description = "Script identifier for replacement; null indicates creation of new script")
  private Long id;

  @Schema(description = "Project identifier for script association; required when creating new script")
  private Long projectId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name for identification and management", example = "script-01", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Flag to enable authorization control for access management")
  public Boolean auth;

  //@NotNull // Compatible with the preservation of legacy data.
  @Schema(description = "Type of platform for test execution.", defaultValue = "API", requiredMode = RequiredMode.REQUIRED)
  @Enumerated(EnumType.STRING)
  private TestPlatform platform = TestPlatform.API;

  @NotNull
  @Schema(description = "Script type defining the testing methodology and execution approach", example = "TEST_PERFORMANCE", requiredMode = RequiredMode.REQUIRED)
  private ScriptType type;

  //@NotNull
  //@Schema(description = "script source", example = "CREATED", requiredMode = RequiredMode.REQUIRED)
  //private ScriptSource source;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Script description for detailed information and requirements")
  private String description;

  @NotBlank
  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @Schema(description = "Script content in YAML or JSON format for execution configuration", requiredMode = RequiredMode.REQUIRED)
  private String content;

}




