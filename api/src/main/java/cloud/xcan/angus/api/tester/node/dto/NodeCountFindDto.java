package cloud.xcan.angus.api.tester.node.dto;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class NodeCountFindDto {

  private Long tenantId;

  private NodeSource source;

  private Boolean enabled;

}



