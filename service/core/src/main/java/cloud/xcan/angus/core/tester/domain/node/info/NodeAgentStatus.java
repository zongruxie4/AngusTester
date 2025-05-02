package cloud.xcan.angus.core.tester.domain.node.info;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeAgentStatus {

  private Long nodeId;

  private Boolean agentOnline;

}
