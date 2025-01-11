package cloud.xcan.sdf.api.angustester.node.vo;


import static cloud.xcan.sdf.spec.utils.NetworkUtils.getDefaultAgentPort;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.angusctrl.node.vo.NodeInfoDetailVo.OsVo;
import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.api.pojo.node.NodeSpecData;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDetailVo {

  private Long id;

  private String name;

  private String ip;

  private String publicIp;

  private String domain;

  private String username;

  private String passd;

  private Integer sshPort;

  private int agentPort = getDefaultAgentPort();

  private NodeSpecData spec;

  private NodeSource source;

  private Set<NodeRole> roles;

  private Boolean freeFlag;

  private LocalDateTime instanceExpiredDate;

  private Boolean enabledFlag;

  private Boolean installAgentFlag;

  private Boolean onlineFlag;

  private Long orderId;

  private boolean isAdmin;

  private OsVo os;

  private Boolean deletedFlag;

  private Long tenantId;

  @NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}



