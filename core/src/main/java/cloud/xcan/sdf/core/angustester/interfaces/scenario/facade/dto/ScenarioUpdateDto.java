package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.AngusScript;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Scenario id", example = "1", required = true)
  private Long id;

  //@NotNull
  //@ApiModelProperty(value = "Project id", example = "1", required = true)
  //private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Scenario name", example = "Create an order")
  private String name;

  //@NotBlank
  //@Length(max = DEFAULT_NAME_LENGTH)
  //@ApiModelProperty(value = "Name of the scenario execution plugin.", example = "Http", required = true)
  //private String pluginName;

  @Length(max = DEFAULT_DESC_LENGTH_X4)
  @ApiModelProperty(value = "Scenario description")
  private String description;

  @ApiModelProperty(value = "Yaml or json format scenario script content")
  private AngusScript script;

  @ApiModelProperty(value = "Whether to enable authorization control, default enabled")
  private Boolean authFlag;

  @ApiModelProperty(value = "Whether to enable functional testing, default enabled")
  private Boolean testFuncFlag;

  @ApiModelProperty(value = "Whether to enable performance testing, default enabled")
  private Boolean testPerfFlag;

  @ApiModelProperty(value = "Whether to enable stability testing, default enabled")
  private Boolean testStabilityFlag;
}




