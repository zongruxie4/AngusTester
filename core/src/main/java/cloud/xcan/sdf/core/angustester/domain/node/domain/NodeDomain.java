package cloud.xcan.sdf.core.angustester.domain.node.domain;


import cloud.xcan.sdf.api.enums.NormalStatus;
import cloud.xcan.sdf.core.jpa.auditor.AuditingEntity;
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


@Entity
@Table(name = "node_domain")
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomain extends AuditingEntity<NodeDomain, Long> {

  @Id
  private Long id;

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private NormalStatus status;

  @Transient
  private int dnsNum;

  @Override
  public Long identity() {
    return this.id;
  }
}
