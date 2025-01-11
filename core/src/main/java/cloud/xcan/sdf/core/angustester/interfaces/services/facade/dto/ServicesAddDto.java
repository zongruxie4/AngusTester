package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

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
public class ServicesAddDto {

  @NotNull
  @ApiModelProperty(value = "Project ID, required when creating a service", required = true)
  private Long projectId;

  @NotBlank
  @ApiModelProperty(example = "UserService", value = "services name", required = true)
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @ApiModelProperty(value = "Whether to enable authorization control, default disabled")
  public Boolean authFlag;
}




