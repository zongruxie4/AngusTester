package cloud.xcan.sdf.core.angustester.domain.apis;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.core.angustester.domain.ResourceFavouriteAndFollow;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ServerConverter;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.spec.http.HttpMethod;
import cloud.xcan.sdf.spec.http.PathMatchers;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApiServerSource;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "apis")
@Where(clause = "deleted_flag = 0 and service_deleted_flag = 0")
@SQLDelete(sql = "update apis set deleted_flag = 1 where id = ?")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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
  @Type(type = "json")
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
   * Note: The available api servers {@link Apis#getAvailableServers()} source {@link
   * ApiServerSource} includes the current request server, api servers, and parent services
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

  @Column(name = "auth_flag")
  public Boolean authFlag;

  @Column(name = "service_auth_flag")
  private Boolean serviceAuthFlag;

  @Column(name = "security_flag")
  public Boolean securityFlag;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  @Column(name = "test_func_flag")
  private Boolean testFuncFlag;

  @Column(name = "test_func_passed_flag")
  private Boolean testFuncPassedFlag;

  @Column(name = "test_func_failure_message")
  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  @Column(name = "test_perf_flag")
  private Boolean testPerfFlag;

  @Column(name = "test_perf_passed_flag")
  private Boolean testPerfPassedFlag;

  @Column(name = "test_perf_failure_message")
  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  @Column(name = "test_stability_flag")
  private Boolean testStabilityFlag;

  @Column(name = "test_stability_passed_flag")
  private Boolean testStabilityPassedFlag;

  @Column(name = "test_stability_failure_message")
  private String testStabilityFailureMessage;

  /**
   * @see cloud.xcan.sdf.core.angustester.domain.services.sync.ProjectSync#name
   */
  @Column(name = "sync_name")
  private String syncName;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Column(name = "service_deleted_flag")
  private Boolean serviceDeletedFlag;

  @Column(name = "deleted_by")
  public Long deletedBy;

  @Column(name = "deleted_flag")
  public Boolean deletedFlag;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "deleted_date", columnDefinition = "TIMESTAMP")
  public LocalDateTime deletedDate;

  @Transient
  private Long mockApisId;
  @Transient
  private Long mockServiceId;
  @Transient
  private Boolean favouriteFlag;
  @Transient
  private Boolean followFlag;
  @Transient
  private Long unarchiveId;
  @Transient
  private String createdByName;
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
