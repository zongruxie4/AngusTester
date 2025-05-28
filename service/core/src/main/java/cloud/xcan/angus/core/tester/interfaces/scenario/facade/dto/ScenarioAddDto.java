package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.AngusScript;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ScenarioAddDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Scenario name", example = "Create an order", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Name of the scenario execution plugin.", example = "Http", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Scenario description")
  private String description;

  @Schema(description = "Yaml or json format scenario script content")
  private AngusScript script;

  @Schema(description = "Whether to enable authorization control, default disabled")
  private Boolean auth;

  @Schema(description = "Whether to enable functional testing, default enabled")
  private Boolean testFunc;

  @Schema(description = "Whether to enable performance testing, default enabled")
  private Boolean testPerf;

  @Schema(description = "Whether to enable stability testing, default enabled")
  private Boolean testStability;

}




