package cloud.xcan.angus.api.tester.node.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class NodeRenewDto {

  @NotNull
  @Schema(description = "New order identifier for node renewal transaction", required = true)
  private Long orderId;

  @NotNull
  @Schema(description = "Original order identifier for renewal reference tracking", required = true)
  private Long originalOrderId;

  @NotNull
  @Schema(description = "Tenant identifier for multi-tenant node renewal", required = true)
  private Long tenantId;

}




