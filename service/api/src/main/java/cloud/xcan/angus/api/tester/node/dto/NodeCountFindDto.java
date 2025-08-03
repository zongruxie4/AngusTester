package cloud.xcan.angus.api.tester.node.dto;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class NodeCountFindDto {

  @Schema(description = "Tenant identifier for multi-tenant node filtering")
  private Long tenantId;

  @Schema(description = "Node source type for infrastructure filtering", example = "CLOUD")
  private NodeSource source;

  @Schema(description = "Node status filter for active/inactive node counting")
  private Boolean enabled;

}



