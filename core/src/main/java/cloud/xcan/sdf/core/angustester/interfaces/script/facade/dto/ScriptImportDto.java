package cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto;


import static cloud.xcan.sdf.spec.experimental.BizConstant.ANGUS_SCRIPT_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author xiaolong.liu
 */
@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScriptImportDto {

  @NotNull
  @ApiModelProperty(value = "Project id, it is required to add script", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Script name", example = "script-01")
  private String name;

  @ApiModelProperty(value = "Script file") // TODO 10MB
  private MultipartFile file;

  @Length(max = ANGUS_SCRIPT_LENGTH) // 10MB
  @ApiModelProperty(value = "Yaml or json format script content")
  private String content;

  @Length(max = DEFAULT_DESC_LENGTH_X4)
  @ApiModelProperty(value = "Script description")
  private String description;
}
