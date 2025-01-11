package cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.angus.model.script.configuration.ScriptType;
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
public class ScriptUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Script id", example = "1", required = true)
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Script name", example = "script-01")
  private String name;

  @ApiModelProperty(value = "Script type, priority is higher than the type in the script content, which controls the actual type of test execution", example = "TEST_PERFORMANCE")
  private ScriptType type;

  //@ApiModelProperty(value = "script source", example = "CREATED")
  //private ScriptSource source;

  @Length(max = DEFAULT_DESC_LENGTH_X4)
  @ApiModelProperty(value = "Script description")
  private String description;

  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @ApiModelProperty(value = "Yaml or json format script content")
  private String content;

}