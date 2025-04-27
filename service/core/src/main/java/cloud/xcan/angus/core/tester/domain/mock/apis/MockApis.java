package cloud.xcan.angus.core.tester.domain.mock.apis;


import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.PathMatchers;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;



@Entity
@Table(name = "mock_apis")
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
  private Boolean inconsistentOperation;
  @Transient
  private Boolean assocApisDeleted;
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
