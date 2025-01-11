package cloud.xcan.sdf.core.angustester.interfaces.node.facade.vo.dns;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.NormalStatus;
import cloud.xcan.sdf.core.angustester.domain.node.dns.DnsLine;
import cloud.xcan.sdf.core.angustester.domain.node.dns.DnsRecordType;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainDnsDetailVo {

  private Long id;

  private Long domainId;

  private DnsRecordType type;

  private String name;

  private DnsLine line;

  private String value;

  private Integer ttl;

  private NormalStatus status;

  private Long tenantId;

  @NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

}
