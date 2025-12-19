package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeInfoFindDto extends PageQuery {

  /**
   * Node Id, Equal to AngusTest node ID
   */
  @Schema(description = "Node identifier for precise query filtering")
  private Long id;

  @Schema(description = "Flag indicating whether agent is installed on the node")
  private Boolean agentInstalled;

  @Schema(description = "Flag indicating whether agent is currently online and responsive")
  private Boolean agentOnline;

  @Schema(description = "Flag indicating whether this is a free/shared node")
  private Boolean isFreeNode;

}
