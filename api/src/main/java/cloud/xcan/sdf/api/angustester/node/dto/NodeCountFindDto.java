package cloud.xcan.sdf.api.angustester.node.dto;

import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeCountFindDto {

  private Long tenantId;

  private NodeSource source;

  private Boolean enabledFlag;

}



