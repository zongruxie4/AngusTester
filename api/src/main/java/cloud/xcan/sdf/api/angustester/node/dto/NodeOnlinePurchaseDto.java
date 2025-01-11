package cloud.xcan.sdf.api.angustester.node.dto;

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
public class NodeOnlinePurchaseDto {

  @NotNull
  @ApiModelProperty(example = "10002929288887", required = true)
  private Long orderId;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long tenantId;

}




