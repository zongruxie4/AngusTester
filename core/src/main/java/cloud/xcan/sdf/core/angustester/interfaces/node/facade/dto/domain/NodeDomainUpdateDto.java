package cloud.xcan.sdf.core.angustester.interfaces.node.facade.dto.domain;

import cloud.xcan.sdf.api.enums.NormalStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainUpdateDto {

  @NotNull
  @ApiModelProperty(required = true)
  private Long id;

  //  @Length(max = DEFAULT_NAME_LENGTH_X2)
  //  @ApiModelProperty(example = "example.com")
  //  private String name;

  @NotNull
  @ApiModelProperty(example = "NORMAL", required = true)
  private NormalStatus status;

}
