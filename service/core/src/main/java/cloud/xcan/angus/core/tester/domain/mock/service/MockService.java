package cloud.xcan.angus.core.tester.domain.mock.service;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import java.util.Set;
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

  @Column(name = "auth")
  private Boolean auth;

  @Column(name = "assoc_service_id")
  private Long assocServiceId;

  @Column(name = "node_id")
  private Long nodeId;

  @Column(name = "node_ip")
  private String nodeIp;

  @Column(name = "service_port")
  private Integer servicePort;

  //@Column(name = "service_ssl_port")
  //private Integer serviceSslPort;

  @Column(name = "service_domain")
  private String serviceDomain;

  @Column(name = "service_dns_id")
  private Long serviceDnsId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "api_security")
  private List<SimpleHttpAuth> apisSecurity;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "api_cors")
  private CorsData apisCors;

  @Type(JsonType.class)
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
    return nonNull(auth) && auth;
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
