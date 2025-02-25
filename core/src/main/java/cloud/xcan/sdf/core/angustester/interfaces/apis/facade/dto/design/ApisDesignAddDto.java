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
public class ApisDesignAddDto {

  @NotNull
  @ApiModelProperty(value = "Project ID", required = true)
  private Long projectId;

  @NotBlank
  @ApiModelProperty(value = "Design name", required = true)
  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @NotBlank
  @ApiModelProperty(value = "Openapi specification version",
      allowableValues = "3.0.0, 3.0.1, 3.0.2, 3.0.3, 3.1.0", example = "3.0.1", required = true)
  private String openapiSpecVersion;

}
