package cloud.xcan.angus.core.tester.domain.node.dns;


import cloud.xcan.angus.api.enums.NormalStatus;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "node_domain_dns")
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainDns extends TenantAuditingEntity<NodeDomainDns, Long> {

  @Id
  private Long id;

  @Column(name = "domain_id")
  private Long domainId;

  /**
   * @see io.netty.handler.codec.dns.DnsRecordType
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private DnsRecordType type;

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "line")
  private DnsLine line;

  @Column(name = "value")
  private String value;

  @Column(name = "ttl")
  private Integer ttl;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private NormalStatus status;

  @Column(name = "cloud_record_id")
  private String cloudRecordId;

  @Override
  public Long identity() {
    return this.id;
  }

}
