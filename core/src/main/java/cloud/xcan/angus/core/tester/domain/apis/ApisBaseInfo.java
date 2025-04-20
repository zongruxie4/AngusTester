package cloud.xcan.angus.core.tester.domain.apis;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiSource;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.converter.ServerConverter;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.PathMatchers;
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
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author XiaoLong Liu
 */@Entity
@Table(name = "apis")
@SQLRestriction("deleted = 0 and service_deleted = 0")
@SQLDelete(sql = "update apis set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ApisBaseInfo extends TenantAuditingEntity<ApisBaseInfo, Long>
    implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  private Long serviceId;

  @Enumerated(EnumType.STRING)
  private ApiSource source;

  /**
   * @see Operation#getSummary()
   */
  private String summary;

  /**
   * @see Operation#getOperationId()
   */
  @Column(name = "operation_id")
  private String operationId;

  @Enumerated(EnumType.STRING)
  private HttpMethod method;

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

  private String endpoint;

  @Enumerated(EnumType.STRING)
  private ApisProtocol protocol;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private ApiStatus status;

  @Column(name = "auth")
  private Boolean auth;

  @Column(name = "service_auth")
  private Boolean serviceAuth;

  private Boolean secured;

  @NonNullable
  @Column(name = "test_func")
  private Boolean testFunc;

  @Column(name = "test_func_passed")
  private Boolean testFuncPassed;

  @NonNullable
  @Column(name = "test_perf")
  private Boolean testPerf;

  @Column(name = "test_perf_passed")
  private Boolean testPerfPassed;

  @NonNullable
  @Column(name = "test_stability")
  private Boolean testStability;

  @Column(name = "test_stability_passed")
  private Boolean testStabilityPassed;

  /**
   * @see `cloud.xcan.angus.core.tester.domain.project.sync.ProjectSync#name`
   */
  @Column(name = "sync_name")
  private String syncName;

  @Column(name = "service_deleted")
  private Boolean serviceDeleted;

  @Column(name = "deleted")
  private Boolean deleted;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  protected Long createdBy;

  @Transient
  private Map<TestType, TaskInfo> testTypeTaskMap;

  public boolean isEnabledAuth() {
    return (nonNull(auth) && auth) || (nonNull(serviceAuth) && serviceAuth);
  }

  public boolean isEnabledTest() {
    return testFunc || testPerf || testStability;
  }

  public boolean isPassedTest() {
    return isEnabledTest() && (
        (!testFunc || nullSafe(testFuncPassed, false)) &&
            (!testPerf || nullSafe(testPerfPassed, false)) &&
            (!testStability || nullSafe(testStabilityPassed, false))
    );
  }

  public int getEnabledTestNum(){
    int testNum = 0;
    if (testFunc){
      testNum++;
    }
    if (testPerf){
      testNum++;
    }
    if (testStability){
      testNum++;
    }
    return testNum;
  }

  public int getPassedTestNum(){
    int passedTestNum = 0;
    if (testFunc && nullSafe(testFuncPassed, false)){
      passedTestNum++;
    }
    if (testPerf && nullSafe(testPerfPassed, false)){
      passedTestNum++;
    }
    if (testStability && nullSafe(testStabilityPassed, false)){
      passedTestNum++;
    }
    return passedTestNum;
  }

  public boolean isWebSocket() {
    return nonNull(protocol) && protocol.isWebSocket();
  }

  public boolean isHttp() {
    return nonNull(protocol) && protocol.isHttp();
  }

  @Override
  public String getName() {
    return this.summary;
  }

  @Override
  public Long getParentId() {
    return serviceId;
  }

  @Override
  public boolean sameIdentityAs(ApisBaseInfo api) {
    return Objects.equals(serviceId, api.serviceId)
        && Objects.equals(method, api.method)
        && PathMatchers.getPathMatcher().match(endpoint, api.endpoint);
  }

  @Override
  public Long identity() {
    return this.id;
  }
}
