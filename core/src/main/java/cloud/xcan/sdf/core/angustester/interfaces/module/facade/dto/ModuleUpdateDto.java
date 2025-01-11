package cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ROOT_PID;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ModuleUpdateDto {

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long id;

  @Min(DEFAULT_ROOT_PID)
  @ApiModelProperty(name = "Parent module ID", example = "1")
  private Long pid;

  @ApiModelProperty(value = "Sorting value, the smaller the value, the higher", example = "10000")
  private Integer sequence;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(name = "Module name", example = "Sprint-1")
  private String name;

}
