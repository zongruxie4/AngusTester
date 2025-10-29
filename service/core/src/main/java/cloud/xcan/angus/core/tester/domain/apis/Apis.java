package cloud.xcan.angus.core.tester.domain.apis;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.ApiResponseConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ExtensionsConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ExternalDocConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.HttpAssertionConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ParameterConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.RequestBodyConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.SecurityRequirementConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.SecuritySchemeConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServerConverter;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServersConverter;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.AngusConstant;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.PathMatchers;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.extension.ApiServerSource;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@JsonInclude(value = Include.NON_EMPTY)
@JsonIncludeProperties(value = {"parameters", "requestBody", "authentication"})
// For extract variables
@Entity
@Table(name = "apis")
@SQLRestriction("deleted = 0 AND service_deleted = 0 ")
@SQLDelete(sql = "update apis set deleted = 1 where id = ?")
@DynamicUpdate
@Setter
@Getter
@Accessors(chain = true)
public class Apis extends TenantAuditingEntity<Apis, Long> implements ActivityResource,
    ResourceFavouriteAndFollow<Apis, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  private Long serviceId;

  @Enumerated(EnumType.STRING)
  private ApiSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "import_source")
  private ApiImportSource importSource;

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

  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  /**
   * @see Operation#getTags()
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "tags")
  private List<String> tags;

  /**
   * @see Operation#getSummary()
   */
  private String summary;

  /**
   * @see Operation#getDescription()
   */
  private String description;

  /**
   * @see Operation#getExternalDocs()
   */
  @Convert(converter = ExternalDocConverter.class)
  @Column(name = "external_docs")
  private ExternalDocumentation externalDocs;

  /**
   * @see Operation#getOperationId()
   */
  @Column(name = "operation_id")
  private String operationId;

  /**
   * @see Operation#getParameters()
   */
  @Convert(converter = ParameterConverter.class)
  @Column(name = "parameters")
  private List<Parameter> parameters;

  /**
   * @see Operation#getRequestBody()
   */
  @Convert(converter = RequestBodyConverter.class)
  @Column(name = "request_body")
  private RequestBody requestBody;

  /**
   * @see Operation#getResponses()
   */
  @Convert(converter = ApiResponseConverter.class)
  @Column(name = "responses")
  private Map<String, ApiResponse> responses;

  /**
   * @see Operation#getDeprecated()
   */
  private Boolean deprecated;

  /**
   * @see Operation#getSecurity()
   */
  @Convert(converter = SecurityRequirementConverter.class)
  @Column(name = "security")
  private List<SecurityRequirement> security;

  /**
   * Note: The available api servers {@link Apis#getAvailableServers()} source
   * {@link ApiServerSource} includes the current request server, api servers, and parent services
   * servers.
   *
   * @see Operation#getServers()
   */
  @Convert(converter = ServerConverter.class)
  @Column(name = "current_server")
  private Server currentServer;

  @Column(name = "current_server_id")
  private Long currentServerId;

  /**
   * Source is {@link ApiServerSource#API_SERVERS}.
   */
  @Convert(converter = ServersConverter.class)
  @Column(name = "servers")
  private List<Server> servers;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @Convert(converter = ExtensionsConverter.class)
  @Column(name = "extensions")
  private Map<String, Object> extensions;
  /////////////////////////OpenAPI Document//////////////////////////

  @Column(name = "schema_hash")
  private int schemaHash;

  @Convert(converter = SecuritySchemeConverter.class)
  @Column(name = "authentication")
  private SecurityScheme authentication;

  @Convert(converter = HttpAssertionConverter.class)
  @Column(name = "assertions")
  private List<Assertion<HttpExtraction>> assertions;

  @Column(name = "owner_id")
  private Long ownerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ApiStatus status;

  @Column(name = "auth")
  private Boolean auth;

  @Column(name = "service_auth")
  private Boolean serviceAuth;

  private Boolean secured;

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

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  @Column(name = "test_func")
  private Boolean testFunc;

  @Column(name = "test_func_passed")
  private Boolean testFuncPassed;

  @Column(name = "test_func_failure_message")
  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  @Column(name = "test_perf")
  private Boolean testPerf;

  @Column(name = "test_perf_passed")
  private Boolean testPerfPassed;

  @Column(name = "test_perf_failure_message")
  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  @Column(name = "test_stability")
  private Boolean testStability;

  @Column(name = "test_stability_passed")
  private Boolean testStabilityPassed;

  @Column(name = "test_stability_failure_message")
  private String testStabilityFailureMessage;
  /**
   * @see `cloud.xcan.angus.core.tester.domain.services.sync.ProjectSync#name`
   */
  @Column(name = "sync_name")
  private String syncName;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Column(name = "service_deleted")
  private Boolean serviceDeleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted")
  private Boolean deleted;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  private LocalDateTime deletedDate;

  @Transient
  private String serviceName;
  @Transient
  private List<Server> availableServers;
  @Transient
  private Map<String, String> resolvedRefModels;
  @Transient
  private Map<String, Tag> tagSchemas;
  @Transient
  private SecurityScheme refAuthentication;
  @Transient
  private Long mockApisId;
  @Transient
  private Long mockServiceId;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private Long unarchivedId;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;
  @Transient
  private Map<TestType, TaskInfo> testTypeTaskMap;

  public boolean isEnabledAuth() {
    return nonNull(auth) && auth;
  }

  public boolean isWebSocket() {
    return nonNull(protocol) && protocol.isWebSocket();
  }

  public boolean isHttp() {
    return nonNull(protocol) && protocol.isHttp();
  }

  public boolean isReleased() {
    return nonNull(status) && status.isReleased();
  }

  public Set<TestType> needTestTypes() {
    Set<TestType> needTested = new HashSet<>();
    if (nonNull(testFunc) && testFunc) {
      needTested.add(TestType.FUNCTIONAL);
    }
    if (nonNull(testPerf) && testPerf) {
      needTested.add(TestType.PERFORMANCE);
    }
    if (nonNull(testStability) && testStability) {
      needTested.add(TestType.STABILITY);
    }
    return needTested;
  }

  public boolean isAuthSchemaRef(){
    return nonNull(authentication) && isNotEmpty(authentication.get$ref());
  }

  public boolean includeSchemaRef(String refKey){
    return isNotEmpty(resolvedRefModels) && resolvedRefModels.containsKey(refKey);
  }

  @Override
  public Long identity() {
    return id;
  }

  @Override
  public Long getParentId() {
    return this.serviceId;
  }

  @Override
  public String getName() {
    return this.summary;
  }

  public String toValueString() {
    return new StringJoiner(", ")
        .add("parameters=" + parameters)
        .add("requestBody=" + requestBody)
        .add("currentServer=" + currentServer)
        .add("authentication=" + authentication)
        .add("assertions=" + assertions)
        .toString();
  }

  /**
   * Use schemaHash() instead.
   */
  @Deprecated
  public boolean sameSchemaInfoAs(Apis o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return Objects.equals(tags, o.tags) &&
        Objects.equals(summary, o.summary) &&
        Objects.equals(description, o.description) &&
        Objects.equals(externalDocs, o.externalDocs) &&
        Objects.equals(operationId, o.operationId) &&
        Objects.equals(parameters, o.parameters) &&
        Objects.equals(requestBody, o.requestBody) &&
        Objects.equals(responses, o.responses) &&
        Objects.equals(deprecated, o.deprecated) &&
        Objects.equals(security, o.security) &&
        // Objects.equals(currentServer, o.currentServer) && -> temp
        // Objects.equals(currentServerId, o.currentServerId) && -> temp
        Objects.equals(servers, o.servers) &&
        Objects.equals(extensions, o.extensions);
    // && Objects.equals(authentication, o.authentication); && -> tmp
  }

  @Override
  public boolean sameIdentityAs(Apis api) {
    return Objects.equals(serviceId, api.serviceId)
        && Objects.equals(method, api.method)
        && PathMatchers.getPathMatcher().match(endpoint, api.endpoint);
  }
}
