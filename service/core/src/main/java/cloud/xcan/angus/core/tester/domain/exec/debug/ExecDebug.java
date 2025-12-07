package cloud.xcan.angus.core.tester.domain.exec.debug;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.pojo.node.NodeInfo;
import cloud.xcan.angus.core.jpa.multitenancy.TenantEntity;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSample;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleContent;
import cloud.xcan.angus.core.tester.infra.metricsds.domain.sample.ExecSampleErrorCause;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.spec.annotations.Nullable;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "exec_debug")
@Setter
@Getter
@Accessors(chain = true)
@EntityListeners({AuditingEntityListener.class})
public class ExecDebug extends TenantEntity<ExecDebug, Long> {

  @Id
  private Long id;

  private String name;

  private String plugin;

  @Enumerated(EnumType.STRING)
  private ExecDebugSource source;

  /**
   * Note: Execute actual script type.
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "script_type")
  private ScriptType scriptType;

  @Nullable
  @Column(name = "script_id")
  private Long scriptId;

  @Column(name = "scenario_id")
  private Long scenarioId;

  @Column(name = "monitor_id")
  private Long monitorId;

  @Enumerated(EnumType.STRING)
  private ExecStatus status;

  private String message;

  /**
   * Note: Last execution node, cannot be cleared, @see isOneNodeTask() usage.
   */
  @Column(name = "exec_node_id")
  private Long execNodeId;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "available_node_ids")
  private LinkedHashSet<Long> availableNodeIds;

  private String script;
  ////////////////////////AngusScript/////////////////////////

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "scheduling_result")
  private RunnerRunVo schedulingResult;

  @Column(name = "meter_status")
  private String meterStatus;

  @Column(name = "meter_message")
  private String meterMessage;

  @Column(name = "single_target_pipeline")
  private Boolean singleTargetPipeline;

  @Column(name = "created_by")
  @CreatedBy
  private Long createdBy;

  @Column(name = "created_date")
  @CreatedDate
  private LocalDateTime createdDate;

  @Transient
  private Configuration configuration;
  @Transient
  private Task task;
  @Transient
  private LinkedHashMap<String, List<String>> pipelineTargetMappings;
  @Transient
  private NodeInfo execNode;
  @Transient
  private List<NodeInfo> availableNodes;
  @Transient
  private ExecDebugDetailVo detailVo;
  @Transient
  private List<ExecSampleContent> sampleContents;
  @Transient
  private List<ExecSampleErrorCause> errorCauses;
  @Transient
  private ExecSample finishSampleResult;

  @Transient
  private Boolean succeed;
  @Transient
  private String failureMessage;

  public boolean isMonitor() {
    return nonNull(monitorId);
  }

  @Override
  public Long identity() {
    return id;
  }

}
