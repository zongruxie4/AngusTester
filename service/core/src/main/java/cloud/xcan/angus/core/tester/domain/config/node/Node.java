package cloud.xcan.angus.core.tester.domain.config.node;


import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.node.NodeSource;
import cloud.xcan.angus.api.enums.NodeRole;
import cloud.xcan.angus.api.pojo.node.NodeSpecData;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.infra.iaas.InstanceChargeType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "node")
@SQLRestriction("deleted = 0")
@Setter
@Getter
@Accessors(chain = true)
public class Node extends TenantAuditingEntity<Node, Long> {

  @Id
  private Long id;

  private String name;

  private String ip;

  private String publicIp;

  private String regionId;

  private String domain;

  private String username;

  private String password;

  private Integer sshPort;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "spec")
  private NodeSpecData spec;

  @Enumerated(EnumType.STRING)
  private NodeSource source;

  private Boolean free;

  private Boolean enabled;

  private String instanceId;

  private String instanceName;

  private LocalDateTime instanceExpiredDate;

  private Boolean deleted;

  private Boolean expired;

  private Long orderId;

  @Enumerated(EnumType.STRING)
  private InstanceChargeType chargeType;

  public Boolean installAgent;

  private Boolean sync;

  /**
   * Search for ID
   */
  private String extSearchMerge;

  @Transient
  private Set<NodeRole> roles;

  public boolean hasFullSshConfig() {
    return (isNotEmpty(this.publicIp) || isNotEmpty(this.ip))
        && isNotEmpty(this.username) && isNotEmpty(this.password)
        && isNotEmpty(this.sshPort);
  }

  public boolean isFreeNode() {
    return nonNull(free) && free;
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
    if (!(o instanceof Node node)) {
      return false;
    }
    return Objects.equals(id, node.id) &&
        Objects.equals(name, node.name) &&
        Objects.equals(ip, node.ip) &&
        Objects.equals(publicIp, node.publicIp) &&
        Objects.equals(regionId, node.regionId) &&
        Objects.equals(domain, node.domain) &&
        Objects.equals(username, node.username) &&
        Objects.equals(password, node.password) &&
        Objects.equals(sshPort, node.sshPort) &&
        Objects.equals(spec, node.spec) &&
        source == node.source &&
        Objects.equals(free, node.free) &&
        Objects.equals(enabled, node.enabled) &&
        Objects.equals(instanceId, node.instanceId) &&
        Objects.equals(instanceName, node.instanceName) &&
        Objects.equals(instanceExpiredDate, node.instanceExpiredDate) &&
        Objects.equals(deleted, node.deleted) &&
        Objects.equals(expired, node.expired) &&
        Objects.equals(orderId, node.orderId) &&
        chargeType == node.chargeType &&
        Objects.equals(installAgent, node.installAgent) &&
        Objects.equals(sync, node.sync);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, ip, publicIp, regionId, domain, username, password, sshPort, spec,
        source, free, enabled, instanceId, instanceName, instanceExpiredDate, deleted,
        expired, orderId, chargeType, installAgent, sync);
  }
}
