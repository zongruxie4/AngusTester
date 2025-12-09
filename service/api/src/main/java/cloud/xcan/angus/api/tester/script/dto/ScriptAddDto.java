package cloud.xcan.angus.api.tester.script.dto;

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


@Setter
@Getter
@Accessors(chain = true)
public class ScriptAddDto {

  @NotNull
  @Schema(description = "Project identifier for script organization and access control", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script display name for identification and organization", example = "script-01", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Authorization control flag for script access management, defaults to disabled")
  public Boolean auth;

  //@NotNull // Compatible with the preservation of legacy data.
  @Schema(description = "Type of platform for test execution.", defaultValue = "API", requiredMode = RequiredMode.REQUIRED)
  @Enumerated(EnumType.STRING)
  private TestPlatform platform = TestPlatform.API;

  @NotNull
  @Schema(description = "Script execution type for test methodology control, overrides content-based type detection", example = "TEST_PERFORMANCE", required = true)
  private ScriptType type;

  //@NotNull
  //@Schema(description = "script source", example = "CREATED", required = true)
  //private ScriptSource source;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Script description for documentation and context")
  private String description;

  @NotBlank
  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @Schema(description = "Script content in YAML or JSON format for test execution configuration", requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

}




