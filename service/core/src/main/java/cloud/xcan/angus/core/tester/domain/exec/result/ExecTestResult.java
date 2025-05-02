package cloud.xcan.angus.core.tester.domain.exec.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.exec.result.ExecSampleResultContent;
import cloud.xcan.angus.api.commonlink.exec.result.ExecTargetSummary;
import cloud.xcan.angus.api.commonlink.exec.result.NodeUsageSummary;
import cloud.xcan.angus.api.gm.indicator.to.FuncTo;
import cloud.xcan.angus.api.gm.indicator.to.PerfTo;
import cloud.xcan.angus.api.gm.indicator.to.StabilityTo;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import cloud.xcan.angus.model.script.ScriptSource;
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
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "exec_test_result")
@Setter
@Getter
@Accessors(chain = true)
public class ExecTestResult extends TenantEntity<ExecTestResult, Long> implements ExecTestResultBase {

  @Id
  @Column(name = "id")
  private Long id;

  @Column(name = "exec_id")
  private Long execId;

  @Enumerated(EnumType.STRING)
  @Column(name = "exec_status")
  private ExecStatus execStatus;

  private String plugin;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Nullable
  @Column(name = "script_id")
  private Long scriptId;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_source")
  private ScriptSource scriptSource;

  @Column(name = "script_source_id")
  private Long scriptSourceId;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "indicator_func")
  private FuncTo indicatorFunc;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "indicator_perf")
  private PerfTo indicatorPerf;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "indicator_stability")
  private StabilityTo indicatorStability;

  private boolean passed;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "test_num")
  private int testNum;

  @Column(name = "test_failure_num")
  private int testFailureNum;

  @Column(name = "usage_failed_node_id")
  private Long usageFailedNodeId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "sample_summary")
  private ExecSampleSummaryInfoVo sampleSummary;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "target_summary")
  private ExecTargetSummary targetSummary;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "case_summary")
  private ExecTargetSummary caseSummary;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "node_usage_summary")
  private Map<Long, NodeUsageSummary> nodeUsageSummary;

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
    this.usageFailedNodeId = usageFailedNodeId;
  }

  /**
   * If apis functional testing.
   */
  @Transient
  private List<ExecTestCaseResult> caseResults;
  @Transient
  private String execName;
  @Transient
  private String execByName;

  @Override
  public Long identity() {
    return this.id;
  }

}
