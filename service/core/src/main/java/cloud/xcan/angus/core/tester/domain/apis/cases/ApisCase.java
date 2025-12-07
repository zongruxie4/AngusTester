package cloud.xcan.angus.core.tester.domain.apis.cases;


import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.RequestBodyConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.SecuritySchemeConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServerConverter;
import cloud.xcan.angus.model.AngusConstant;
import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "apis_case")
@SQLRestriction("apis_deleted=0")
@Setter
@Getter
@Accessors(chain = true)
public class ApisCase extends TenantAuditingEntity<ApisCase, Long> implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "services_id")
  private Long servicesId;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "apis_id")
  private Long apisId;

  @Column(name = "enabled")
  private Boolean enabled;

  @Enumerated(EnumType.STRING)
  private ApisCaseType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "test_method")
  private CaseTestMethod testMethod;

  /**
   * @see Operation#getServers()
   */
  @Enumerated(EnumType.STRING)
  private ApisProtocol protocol;

  /**
   * @see PathItem
   */
  @Enumerated(EnumType.STRING)
  private HttpMethod method;

  /**
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  private String endpoint;

  @Convert(converter = ServerConverter.class)
  @Column(name = "current_server")
  private Server currentServer;

  /**
   * @see Operation#getParameters()
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "parameters")
  private List<Parameter> parameters;

  /**
   * @see Operation#getRequestBody()
   */
  @Convert(converter = RequestBodyConverter.class)
  @Column(name = "request_body")
  private RequestBody requestBody;

  @Convert(converter = SecuritySchemeConverter.class)
  @Column(name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  /**
   * Process actions when the dataset reaches the end of reading, default `RECYCLE`.
   *
   * @see AngusConstant#DEFAULT_ACTION_ON_EOF
   */
  @Column(name = "dataset_action_on_eof")
  @Enumerated(EnumType.STRING)
  private ActionOnEOF datasetActionOnEOF;

  /**
   * Dataset sharing mode when multi threads, default `ALL_THREAD`.
   *
   * @see AngusConstant#DEFAULT_SHARING_MODE
   */
  @Column(name = "dataset_sharing_mode")
  @Enumerated(EnumType.STRING)
  private SharingMode datasetSharingMode;

  @Column(name = "apis_deleted")
  private Boolean apisDeleted;

  @Column(name = "exec_result")
  @Enumerated(EnumType.STRING)
  private Result execResult;

  @Column(name = "exec_failure_message")
  private String execFailureMessage;

  @Column(name = "exec_test_num")
  private Integer execTestNum;

  @Column(name = "exec_test_failure_num")
  private Integer execTestFailureNum;

  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "exec_name")
  private String execName;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "exec_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime execDate;

  @Transient
  private List<Server> availableServers;
  @Transient
  private Map<String, String> resolvedRefModels;
  @Transient
  private SecurityScheme refAuthentication;

  @Transient
  private String apisSummary;
  @Transient
  private Long apisServiceId;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;

  public boolean isAuthSchemaRef() {
    return nonNull(authentication) && isNotEmpty(authentication.get$ref());
  }

  public boolean includeSchemaRef(String refKey) {
    return isNotEmpty(resolvedRefModels) && resolvedRefModels.containsKey(refKey);
  }

  @Override
  public Long getParentId() {
    return apisId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
