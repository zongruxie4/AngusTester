package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.exec.result.AssertionSummary;
import cloud.xcan.angus.api.commonlink.exec.result.ExecApisResultInfo;
import cloud.xcan.angus.api.commonlink.exec.result.ExecSampleResultContent;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.annotations.Nullable;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "exec_test_case_result")
@Setter
@Getter
@Accessors(chain = true)
public class ExecTestCaseResult extends TenantEntity<ExecTestCaseResult, Long> implements
    ExecTestResultBase {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "exec_id")
  private Long execId;

  @Enumerated(EnumType.STRING)
  @Column(name = "exec_status")
  private ExecStatus execStatus;

  private String plugin;

  // Only functional test data
  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Nullable
  @Column(name = "script_id")
  private Long scriptId;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  @Column(name = "apis_id")
  private Long apisId;

  @Column(name = "case_id")
  private Long caseId;

  @Column(name = "case_name")
  private String caseName;

  //@Column(name = "parent_id") -> Changes will occur after movement
  //private Long parentId;

  private Boolean enabled;

  @Column(name = "case_type")
  private ApisCaseType caseType;

  /**
   * Note: A null value means not executed, disabled.
   */
  private Boolean passed;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "test_num")
  private int testNum;

  @Column(name = "test_failure_num")
  private int testFailureNum;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "assertion_summary")
  private AssertionSummary assertionSummary;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "sample_content")
  private List<ExecSampleResultContent> sampleContent;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "last_exec_date")
  private LocalDateTime lastExecDate;

  @Column(name = "created_date")
  private LocalDateTime createdDate;

  @Override
  public void setPassed(Boolean passed) {
    this.passed = passed;
  }

  @Override
  public void setFailureMessage(String failureMessage) {
    this.failureMessage = failureMessage;
  }

  @Override
  public void setUsageFailedNodeId(Long usageFailedNodeId) {
  }

  /**
   * If apis functional testing.
   */
  @Transient
  private ExecApisResultInfo apisCaseResult;
  @Transient
  private String execName;
  @Transient
  private String execByName;

  @Override
  public Long identity() {
    return this.id;
  }
}
