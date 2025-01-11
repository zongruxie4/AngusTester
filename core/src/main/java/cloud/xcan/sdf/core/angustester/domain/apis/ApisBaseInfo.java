package cloud.xcan.sdf.core.angustester.domain.apis;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.apis.ApiSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.apis.converter.ServerConverter;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.sdf.spec.annotations.NonNullable;
import cloud.xcan.sdf.spec.http.HttpMethod;
import cloud.xcan.sdf.spec.http.PathMatchers;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApiServerSource;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author xiaolong.liu
 */
@Entity
@Table(name = "apis")
@Where(clause = "deleted_flag = 0 and service_deleted_flag = 0")
@SQLDelete(sql = "update apis set deleted_flag = 1 where id = ?")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Column(name = "auth_flag")
  private Boolean authFlag;

  @Column(name = "service_auth_flag")
  private Boolean serviceAuthFlag;

  @Column(name = "security_flag")
  private Boolean securityFlag;

  @NonNullable
  @Column(name = "test_func_flag")
  private Boolean testFuncFlag;

  @Column(name = "test_func_passed_flag")
  private Boolean testFuncPassedFlag;

  @NonNullable
  @Column(name = "test_perf_flag")
  private Boolean testPerfFlag;

  @Column(name = "test_perf_passed_flag")
  private Boolean testPerfPassedFlag;

  @NonNullable
  @Column(name = "test_stability_flag")
  private Boolean testStabilityFlag;

  @Column(name = "test_stability_passed_flag")
  private Boolean testStabilityPassedFlag;

  /**
   * @see `cloud.xcan.sdf.core.angustester.domain.project.sync.ProjectSync#name`
   */
  @Column(name = "sync_name")
  private String syncName;

  @Column(name = "service_deleted_flag")
  private Boolean serviceDeletedFlag;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

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
    return (nonNull(authFlag) && authFlag) || (nonNull(serviceAuthFlag) && serviceAuthFlag);
  }

  public boolean isEnabledTest() {
    return testFuncFlag || testPerfFlag || testStabilityFlag;
  }

  public boolean isPassedTest() {
    return isEnabledTest() && (
        (!testFuncFlag || nullSafe(testFuncPassedFlag, false)) &&
            (!testPerfFlag || nullSafe(testPerfPassedFlag, false)) &&
            (!testStabilityFlag || nullSafe(testStabilityPassedFlag, false))
    );
  }

  public int getEnabledTestNum(){
    int testNum = 0;
    if (testFuncFlag){
      testNum++;
    }
    if (testPerfFlag){
      testNum++;
    }
    if (testStabilityFlag){
      testNum++;
    }
    return testNum;
  }

  public int getPassedTestNum(){
    int passedTestNum = 0;
    if (testFuncFlag && nullSafe(testFuncPassedFlag, false)){
      passedTestNum++;
    }
    if (testPerfFlag && nullSafe(testPerfPassedFlag, false)){
      passedTestNum++;
    }
    if (testStabilityFlag && nullSafe(testStabilityPassedFlag, false)){
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
