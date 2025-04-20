package cloud.xcan.angus.api.tester.node.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class NodeOnlinePurchaseDto {

  @NotNull
  @Schema(example = "10002929288887", required = true)
  private Long orderId;

  @NotNull
  @Schema(required = true)
  private Long tenantId;

}




