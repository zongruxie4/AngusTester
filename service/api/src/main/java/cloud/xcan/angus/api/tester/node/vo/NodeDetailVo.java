package cloud.xcan.angus.api.tester.node.vo;


import static cloud.xcan.angus.spec.utils.NetworkUtils.getDefaultAgentPort;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.api.tester.node.vo.NodeInfoDetailVo.OsVo;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


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

  private String password;

  private Integer sshPort;

  private int agentPort = getDefaultAgentPort();

  private NodeSpecData spec;

  private NodeSource source;

  private Set<NodeRole> roles;

  private Boolean free;

  private LocalDateTime instanceExpiredDate;

  private Boolean enabled;

  private Boolean installAgent;

  private Boolean online;

  private Long orderId;

  private boolean isAdmin;

  private OsVo os;

  private Boolean deleted;

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



