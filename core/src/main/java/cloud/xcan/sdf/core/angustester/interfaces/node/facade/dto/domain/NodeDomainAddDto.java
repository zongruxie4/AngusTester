package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainAddDto {

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(example = "example.com", required = true)
  private String name;

}




