package cloud.xcan.angus.api.tester.script.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Project id", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Script name", example = "script-01", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Whether to enable authorization control, default disabled")
  public Boolean auth;

  @NotNull
  @Schema(description = "Script type, priority is higher than the type in the script content, which controls the actual type of test execution", example = "TEST_PERFORMANCE", required = true)
  private ScriptType type;

  //@NotNull
  //@Schema(description = "script source", example = "CREATED", required = true)
  //private ScriptSource source;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Script description")
  private String description;

  @NotBlank
  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @Schema(description = "Yaml or json format script content", requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

}




