package cloud.xcan.sdf.core.angustester.interfaces.module.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ROOT_PID;

import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashSet;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ModuleAddDto {

  @NotNull
  @ApiModelProperty(name = "Project id", example = "1", required = true)
  private Long projectId;

  @Min(DEFAULT_ROOT_PID)
  @ApiModelProperty(name = "Parent module ID, null and -1 indicate that it is a top-level node", example = "-1")
  private Long pid;

  @ApiModelProperty(value = "Sorting value, the smaller the value, the higher", example = "10000", required = true)
  private Integer sequence;

  @NotEmpty
  @ApiModelProperty(name = "Module name", required = true)
  private LinkedHashSet<String> names;

}
