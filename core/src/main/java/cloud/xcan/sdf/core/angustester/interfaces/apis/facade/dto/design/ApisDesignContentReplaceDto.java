package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.design;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_OPENAPI_LENGTH;

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
public class ApisDesignContentReplaceDto {

  @NotNull
  @ApiModelProperty(value = "Design ID", required = true)
  private Long id;

  @NotNull
  @ApiModelProperty(value = "Design content", required = true)
  @Length(max = MAX_OPENAPI_LENGTH)
  private String openapi;

}
