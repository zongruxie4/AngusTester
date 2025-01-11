package cloud.xcan.sdf.core.angustester.domain.scenario;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;

import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.exec.ExecStatus;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
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
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "scenario")
@Where(clause = "deleted_flag = 0 ")
@SQLDelete(sql = "update scenario set deleted_flag = 1 where id = ?")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Column(name = "auth_flag")
  private Boolean authFlag;

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

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

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
  private Boolean favouriteFlag;
  @Transient
  private Boolean followFlag;
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
    return Objects.nonNull(authFlag) && authFlag;
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

  public int getEnabledTestNum() {
    int testNum = 0;
    if (testFuncFlag) {
      testNum++;
    }
    if (testPerfFlag) {
      testNum++;
    }
    if (testStabilityFlag) {
      testNum++;
    }
    return testNum;
  }

  public int getPassedTestNum() {
    int passedTestNum = 0;
    if (testFuncFlag && nullSafe(testFuncPassedFlag, false)) {
      passedTestNum++;
    }
    if (testPerfFlag && nullSafe(testPerfPassedFlag, false)) {
      passedTestNum++;
    }
    if (testStabilityFlag && nullSafe(testStabilityPassedFlag, false)) {
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
