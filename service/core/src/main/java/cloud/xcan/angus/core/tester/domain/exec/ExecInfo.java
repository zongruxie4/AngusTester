package cloud.xcan.angus.core.tester.domain.exec;

import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.spec.annotations.Nullable;
import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "exec")
@Setter
@Getter
@Accessors(chain = true)
@EntityListeners({AuditingEntityListener.class})
public class ExecInfo extends TenantAuditingEntity<ExecInfo, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "service_id")
  private Long serviceId;

  private String name;

  private String plugin;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Nullable
  @Column(name = "script_id")
  private Long scriptId;

  @Column(name = "script_created_by")
  private Long scriptCreatedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "script_source")
  private ScriptSource scriptSource;

  @Column(name = "script_source_id")
  private Long scriptSourceId;

  @Enumerated(EnumType.STRING)
  private ExecStatus status;

  /////////////////////////////Redundant script field///////////////////////////
  //-------Fields that need to be overwritten for script configuration--------//
  private Long iterations;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "duration")
  private TimeValue duration;

  private int thread;

  private int priority;

  @Column(name = "ignore_assertions")
  private Boolean ignoreAssertions;

  @Enumerated(EnumType.STRING)
  @Column(name = "start_mode")
  private StartMode startMode;

  @Column(name = "start_at_date")
  private LocalDateTime startAtDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "startup_timeout")
  private TimeValue startupTimeout;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "report_interval")
  private TimeValue reportInterval;

  //-------Fields that need to be overwritten for script configuration--------//

  @Column(name = "update_test_result")
  private Boolean updateTestResult;

  @Column(name = "sync_test_result")
  private boolean syncTestResult;

  @Column(name = "sync_test_result_failure")
  private String syncTestResultFailure;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "assoc_api_case_ids")
  private Set<Long> assocApiCaseIds;

  /**
   * Note: Last execution node, cannot be cleared, @see isOneNodeTask() usage.
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "exec_node_ids")
  private LinkedHashSet<Long> execNodeIds;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "available_node_ids")
  private LinkedHashSet<Long> availableNodeIds;

  //private String script;
  ////////////////////////AngusScript/////////////////////////

  private Boolean trial;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "actual_start_date")
  private LocalDateTime actualStartDate;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "exec_by")
  private Long execBy;

  @Column(name = "scheduling_num")
  private int schedulingNum;

  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "last_scheduling_date")
  private LocalDateTime lastSchedulingDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "last_scheduling_result")
  private List<RunnerRunVo> lastSchedulingResult;

  /**
   * ========MeterResult=======
   */
  @Column(name = "meter_status")
  private String meterStatus;

  @Column(name = "meter_message")
  private String meterMessage;

  @Column(name = "single_target_pipeline")
  private Boolean singleTargetPipeline;

  /**
   * Search for ID
   */
  @Column(name = "ext_search_merge")
  private String extSearchMerge;

  @Transient
  private ExecSample finishSample;
  @Transient
  private String scriptName;
  @Transient
  private String scriptSourceName;
  @Transient
  private boolean hasOperationPermission;

  public boolean canUpdateTestResult(){
    return PLUGIN_HTTP_NAME.equals(plugin) && nonNull(scriptSource)
        && nonNull(scriptSourceId) && scriptSource.needTestResult();
  }

  @JsonIgnore
  public boolean isSingleTargetPipeline() {
    return nonNull(singleTargetPipeline) && singleTargetPipeline;
  }

  @JsonIgnore
  public boolean isOneNodeTask() {
    return isNotEmpty(execNodeIds) && execNodeIds.size() == 1;
  }

  @Override
  public Long identity() {
    return id;
  }
}
