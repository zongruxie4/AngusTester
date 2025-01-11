package cloud.xcan.sdf.core.angustester.domain.node;


import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.api.commonlink.node.NodeSource;
import cloud.xcan.sdf.api.enums.NodeRole;
import cloud.xcan.sdf.api.pojo.node.NodeSpecData;
import cloud.xcan.sdf.core.angustester.infra.iaas.InstanceChargeType;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "node")
@Where(clause = "deleted_flag = 0")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class Node extends TenantAuditingEntity<Node, Long> {

  @Id
  private Long id;

  @Column(name = "name")
  private String name;

  @Column(name = "ip")
  private String ip;

  @Column(name = "public_ip")
  private String publicIp;

  @Column(name = "region_id")
  private String regionId;

  @Column(name = "domain")
  private String domain;

  @Column(name = "username")
  private String username;

  @Column(name = "passd")
  private String passd;

  @Column(name = "ssh_port")
  private Integer sshPort;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "spec")
  private NodeSpecData spec;

  @Enumerated(EnumType.STRING)
  @Column(name = "source")
  private NodeSource source;

  @Column(name = "free_flag")
  private Boolean freeFlag;

  @Column(name = "enabled_flag")
  private Boolean enabledFlag;

  @Column(name = "instance_id")
  private String instanceId;

  @Column(name = "instance_name")
  private String instanceName;

  @Column(name = "instance_expired_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime instanceExpiredDate;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

  @Column(name = "expired_flag")
  private Boolean expiredFlag;

  @Column(name = "order_id")
  private Long orderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "charge_type")
  private InstanceChargeType chargeType;

  @Column(name = "install_agent_flag")
  public Boolean installAgentFlag;

  @Column(name = "sync_flag")
  private Boolean syncFlag;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Transient
  private Set<NodeRole> roles;

  public boolean hasFullSshConfig() {
    return (isNotEmpty(this.publicIp) || isNotEmpty(this.ip))
        && isNotEmpty(this.username) && isNotEmpty(this.passd)
        && isNotEmpty(this.sshPort);
  }

  public boolean isFreeNode() {
    return nonNull(freeFlag) && freeFlag;
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Node)) {
      return false;
    }
    Node node = (Node) o;
    return Objects.equals(id, node.id) &&
        Objects.equals(name, node.name) &&
        Objects.equals(ip, node.ip) &&
        Objects.equals(publicIp, node.publicIp) &&
        Objects.equals(regionId, node.regionId) &&
        Objects.equals(domain, node.domain) &&
        Objects.equals(username, node.username) &&
        Objects.equals(passd, node.passd) &&
        Objects.equals(sshPort, node.sshPort) &&
        Objects.equals(spec, node.spec) &&
        source == node.source &&
        Objects.equals(freeFlag, node.freeFlag) &&
        Objects.equals(enabledFlag, node.enabledFlag) &&
        Objects.equals(instanceId, node.instanceId) &&
        Objects.equals(instanceName, node.instanceName) &&
        Objects.equals(instanceExpiredDate, node.instanceExpiredDate) &&
        Objects.equals(deletedFlag, node.deletedFlag) &&
        Objects.equals(expiredFlag, node.expiredFlag) &&
        Objects.equals(orderId, node.orderId) &&
        chargeType == node.chargeType &&
        Objects.equals(installAgentFlag, node.installAgentFlag) &&
        Objects.equals(syncFlag, node.syncFlag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, ip, publicIp, regionId, domain, username, passd, sshPort, spec,
        source, freeFlag, enabledFlag, instanceId, instanceName, instanceExpiredDate, deletedFlag,
        expiredFlag, orderId, chargeType, installAgentFlag, syncFlag);
  }
}
