package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.AngusScript;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ScenarioReplaceDto {

  @Schema(description = "Scenario identifier for replacement; null indicates creation of new scenario")
  private Long id;

  @Schema(description = "Project identifier for scenario association; required when creating new scenario")
  private Long projectId;

  @Schema(description = "Scenario module identifier for categorization")
  private Long moduleId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Scenario name for identification and management", example = "Create an order", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotBlank
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Scenario execution plugin name defining the testing protocol", example = "Http", requiredMode = RequiredMode.REQUIRED)
  private String plugin;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Scenario description for detailed information and requirements")
  private String description;

  @Schema(description = "Scenario script content in YAML or JSON format for execution configuration")
  private AngusScript script;

  @Schema(description = "Flag to enable authorization control for access management")
  private Boolean auth;

  @Schema(description = "Flag to enable functional testing capabilities")
  private Boolean testFunc;

  @Schema(description = "Flag to enable performance testing capabilities")
  private Boolean testPerf;

  @Schema(description = "Flag to enable stability testing capabilities")
  private Boolean testStability;
}




