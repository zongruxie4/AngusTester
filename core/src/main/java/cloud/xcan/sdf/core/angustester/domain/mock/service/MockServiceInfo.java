package cloud.xcan.sdf.core.angustester.domain.mock.service;


import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "mock_service")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
public class MockServiceInfo extends TenantAuditingEntity<MockServiceInfo, Long> implements
    ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "name")
  private String name;

  @Column(name = "source")
  @Enumerated(EnumType.STRING)
  private MockServiceSource source;

  @Column(name = "import_source")
  @Enumerated(EnumType.STRING)
  private ApiImportSource importSource;

  @Column(name = "auth_flag")
  private Boolean authFlag;

  @Column(name = "assoc_service_id")
  private Long assocServiceId;

  @Column(name = "node_id")
  private Long nodeId;

  @Column(name = "service_port")
  private Integer servicePort;

  //@Column(name = "service_ssl_port")
  //private Integer serviceSslPort;

  @Column(name = "service_domain")
  private String serviceDomain;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  private transient String nodeIp;
  private transient String nodePublicIp;
  private transient String serviceUrl;
  private transient MockServiceStatus serviceStatus;
  private transient Set<MockServicePermission> currentAuths;

  public String getServiceDomainUrl() {
    return isEmpty(serviceDomain) ? null : (80 == servicePort ? "http://" + serviceDomain
        : "http://" + serviceDomain + ":" + servicePort);
  }

  public String getServiceHostUrl() {
    return isEmpty(nodeIp) ? null
        : (80 == servicePort ? "http://" + nodeIp : "http://" + nodeIp + ":" + servicePort);
  }

  @Override
  public Long getParentId() {
    return null;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
