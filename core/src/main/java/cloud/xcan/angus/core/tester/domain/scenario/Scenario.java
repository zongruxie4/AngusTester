package cloud.xcan.angus.core.tester.domain.scenario;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import jakarta.persistence.Column;
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
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "scenario")
@SQLRestriction("deleted = 0 ")
@SQLDelete(sql = "update scenario set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class Scenario extends TenantAuditingEntity<Scenario, Long> implements ActivityResource {

  @Id
  private Long id;

  private String name;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "auth")
  private Boolean auth;

  private String plugin;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Column(name = "script_id")
  private Long scriptId;

  private String description;

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

  @Column(name = "deleted")
  private Boolean deleted;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deletedDate;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Transient
  private String scriptName;
  @Transient
  private AngusScript angusScript;
  @Transient
  private Boolean favourite;
  @Transient
  private Boolean follow;
  @Transient
  private String createdByName;
  @Transient
  private String avatar;
  @Transient
  private Map<TestType, TaskInfo> testTypeTaskMap;

  @Transient
  private Long lastExecId;
  @Transient
  private ExecStatus lastExecStatus;
  @Transient
  private String lastExecFailureMessage;

  public boolean isEnabledAuth() {
    return Objects.nonNull(auth) && auth;
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

  public int getEnabledTestNum() {
    int testNum = 0;
    if (testFunc) {
      testNum++;
    }
    if (testPerf) {
      testNum++;
    }
    if (testStability) {
      testNum++;
    }
    return testNum;
  }

  public int getPassedTestNum() {
    int passedTestNum = 0;
    if (testFunc && nullSafe(testFuncPassed, false)) {
      passedTestNum++;
    }
    if (testPerf && nullSafe(testPerfPassed, false)) {
      passedTestNum++;
    }
    if (testStability && nullSafe(testStabilityPassed, false)) {
      passedTestNum++;
    }
    return passedTestNum;
  }

  @Override
  public Long identity() {
    return this.id;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
