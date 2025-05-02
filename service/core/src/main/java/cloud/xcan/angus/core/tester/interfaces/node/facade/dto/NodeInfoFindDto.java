package cloud.xcan.angus.core.tester.interfaces.node.facade.dto;

import cloud.xcan.angus.remote.PageQuery;
import java.time.LocalDateTime;
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
  private Long id;

  private Boolean agentInstalled;

  private Boolean agentOnline;

  private Boolean isFreeNode;

  private LocalDateTime lastModifiedDate;

}
