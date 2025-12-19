package cloud.xcan.angus.core.tester.domain.apis;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServerConverter;
import cloud.xcan.angus.core.tester.domain.issue.TaskInfo;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.PathMatchers;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApiServerSource;
import io.swagger.v3.oas.models.extension.ApisProtocol;
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
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "apis")
@SQLRestriction("deleted = 0 and service_deleted = 0")
@SQLDelete(sql = "update apis set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class ApisBasicInfo extends TenantAuditingEntity<ApisBasicInfo, Long>
    implements ActivityResource, ResourceFavouriteAndFollow<ApisBasicInfo, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  public Long serviceId;

  @Enumerated(EnumType.STRING)
  public ApiSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "import_source")
  public ApiImportSource importSource;

  /**
   * @see Operation#getSummary()
   */
  private String summary;

  /**
   * @see Operation#getTags()
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "tags")
  private List<String> tags;

  /**
   * @see Operation#getOperationId()
   */
  @Column(name = "operation_id")
  private String operationId;

  @Enumerated(EnumType.STRING)
  public HttpMethod method;

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

  public String endpoint;

  @Enumerated(EnumType.STRING)
  public ApisProtocol protocol;

  public String description;

  /**
   * @see Operation#getDeprecated()
   */
  private Boolean deprecated;

  @Column(name = "owner_id")
  private Long ownerId;

  @Enumerated(EnumType.STRING)
  public ApiStatus status;

  @Column(name = "auth")
  public Boolean auth;

  @Column(name = "service_auth")
  private Boolean serviceAuth;

  public Boolean secured;

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
  public Long deletedBy;

  @Column(name = "deleted")
  public Boolean deleted;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  public LocalDateTime deletedDate;

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
  private String creator;
  @Transient
  private String avatar;
  @Transient
  private Map<TestType, TaskInfo> testTypeTaskMap;

  public boolean isReleased() {
    return nonNull(status) && status.isReleased();
  }

  @Override
  public String getName() {
    return summary;
  }

  @Override
  public Long getParentId() {
    return serviceId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public boolean sameIdentityAs(ApisBasicInfo api) {
    return Objects.equals(serviceId, api.serviceId)
        && Objects.equals(method, api.method)
        && PathMatchers.getPathMatcher().match(endpoint, api.endpoint);
  }
}
