package cloud.xcan.angus.core.tester.domain.node.domain;


import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.jpa.auditor.AuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
