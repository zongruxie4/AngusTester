package cloud.xcan.sdf.core.angustester.domain.mock.service;


import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.pojo.CorsData;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.remoting.MockServiceSetting;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;


@Entity
@Table(name = "mock_service")
@Setter
@Getter
@Accessors(chain = true)
public class MockService extends TenantAuditingEntity<MockService, Long> implements
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

  @Column(name = "node_ip")
  private  String nodeIp;

  @Column(name = "service_port")
  private Integer servicePort;

  //@Column(name = "service_ssl_port")
  //private Integer serviceSslPort;

  @Column(name = "service_domain")
  private String serviceDomain;

  @Column(name = "service_dns_id")
  private Long serviceDnsId;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "api_security")
  private List<SimpleHttpAuth> apisSecurity;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "api_cors")
  private CorsData apisCors;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "setting")
  private MockServiceSetting setting;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  private transient String nodePublicIp;
  // @Transient -> transient <- Json and jpa both ignore
  private transient Set<Long> apiIds;
  private transient MultipartFile importFile;
  private transient String importText;
  private transient MockServiceStatus serviceStatus;
  private transient Set<MockServicePermission> currentAuths;

  @JsonIgnore // Ignore Cache
  @Transient
  public String getServiceDomainUrl() {
    return isEmpty(serviceDomain) ? null : (80 == servicePort ? "http://" + serviceDomain
        : "http://" + serviceDomain + ":" + servicePort);
  }

  @JsonIgnore // Ignore Cache
  @Transient
  public String getServiceHostUrl() {
    return isEmpty(nodeIp) ? null : (80 == servicePort ? "http://" + nodeIp
        : "http://" + nodeIp + ":" + servicePort);
  }

  @JsonIgnore // Ignore Cache
  @Transient
  public boolean isEnabledAuth() {
    return nonNull(authFlag) && authFlag;
  }

  @JsonIgnore // Ignore Cache
  @Override
  public Long getParentId() {
    return projectId;
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
