package cloud.xcan.sdf.api.angustester.script.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
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
public class ScriptAddDto {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Script name", example = "script-01", required = true)
  private String name;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  public Boolean authFlag;

  @NotNull
  @ApiModelProperty(value = "Script type, priority is higher than the type in the script content, which controls the actual type of test execution", example = "TEST_PERFORMANCE", required = true)
  private ScriptType type;

  //@NotNull
  //@ApiModelProperty(value = "script source", example = "CREATED", required = true)
  //private ScriptSource source;

  @Length(max = DEFAULT_DESC_LENGTH_X4)
  @ApiModelProperty(value = "Script description")
  private String description;

  @NotBlank
  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @ApiModelProperty(value = "Yaml or json format script content", required = true)
  private String content;

}




