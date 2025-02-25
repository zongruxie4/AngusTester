package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design;

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
public class ApisDesignUpdateNameDto {

  @NotNull
  @ApiModelProperty(value = "Design ID", required = true)
  private Long id;

  @NotBlank
  @ApiModelProperty(value = "Design name", required = true)
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

}
