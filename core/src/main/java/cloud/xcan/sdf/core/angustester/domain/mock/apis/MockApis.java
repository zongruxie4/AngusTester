package cloud.xcan.sdf.core.angustester.domain.mock.apis;


import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.spec.http.HttpMethod;
import cloud.xcan.sdf.spec.http.PathMatchers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
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
import org.hibernate.annotations.TypeDef;


@Entity
@Table(name = "mock_apis")
@TypeDef(name = "json", typeClass = JsonStringType.class)
@Setter
@Getter
@Accessors(chain = true)
public class MockApis extends TenantAuditingEntity<MockApis, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "summary")
  private String summary;

  @Column(name = "description")
  private String description;

  @Column(name = "source")
  @Enumerated(EnumType.STRING)
  private MockApisSource source;

  @Column(name = "import_source")
  @Enumerated(EnumType.STRING)
  private ApiImportSource importSource;

  @Column(name = "method")
  @Enumerated(EnumType.STRING)
  private HttpMethod method;

  @Column(name = "endpoint")
  private String endpoint;

  @Column(name = "mock_service_id")
  private Long mockServiceId;

  @Column(name = "assoc_service_id")
  private Long assocServiceId;

  @Column(name = "assoc_apis_id")
  private Long assocApisId;

  @Column(name = "request_num")
  private long requestNum;

  @Column(name = "pushback_num")
  private long pushbackNum;

  @Column(name = "simulate_error_num")
  private long simulateErrorNum;

  @Column(name = "success_num")
  private long successNum;

  @Column(name = "exception_num")
  private long exceptionNum;

  @Transient
  private String url;
  @Transient
  private List<MockApisResponse> responses;
  @Transient
  private Boolean inconsistentOperationFlag;
  @Transient
  private Boolean assocApisDeletedFlag;
  @Transient
  private String assocApisName;
  @Transient
  private HttpMethod apisMethod;
  @Transient
  private String apisEndpoint;
  @Transient
  private String mockServiceName;
  @Transient
  private Long parentMockServiceId;
  @Transient
  private String parentMockServiceName;
  @Transient
  private String mockServiceDomainUrl;
  @Transient
  private String mockServiceHostUrl;

  @Override
  public boolean sameIdentityAs(MockApis that) {
    return Objects.equals(method, that.method)
        && PathMatchers.getPathMatcher().match(endpoint, that.endpoint);
  }

  public boolean sameIdentityAs(cloud.xcan.angus.model.element.mock.apis.MockApis that) {
    return Objects.equals(method.name(), that.getMethod().name())
        && PathMatchers.getPathMatcher().match(endpoint, that.getEndpoint());
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @JsonIgnore
  @Override
  public String getName() {
    return summary;
  }

  @JsonIgnore
  @Override
  public Long getParentId() {
    return mockServiceId;
  }

  @Override
  public Long getProjectId() {
    return projectId;
  }
}
