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

  private Long projectId;

  private Long serviceId;

  private String name;

  private String plugin;

  @Enumerated(EnumType.STRING)
  private ScriptType scriptType;

  @Nullable
  private Long scriptId;

  private Long scriptCreatedBy;

  @Enumerated(EnumType.STRING)
  private ScriptSource scriptSource;

  private Long scriptSourceId;

  @Enumerated(EnumType.STRING)
  private ExecStatus status;

  /////////////////////////////Redundant script field///////////////////////////
  //-------Fields that need to be overwritten for script configuration--------//
  private Long iterations;

  private TimeValue duration;

  private int thread;

  private int priority;

  private Boolean ignoreAssertions;

  @Enumerated(EnumType.STRING)
  private StartMode startMode;

  private LocalDateTime startAtDate;

  private TimeValue startupTimeout;

  private TimeValue reportInterval;

  //-------Fields that need to be overwritten for script configuration--------//

  private Boolean updateTestResult;

  private boolean syncTestResult;

  private String syncTestResultFailure;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private Set<Long> assocApiCaseIds;

  /**
   * Note: Last execution node, cannot be cleared, @see isOneNodeTask() usage.
   */
  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private LinkedHashSet<Long> execNodeIds;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private LinkedHashSet<Long> availableNodeIds;

  //private String script;
  ////////////////////////AngusScript/////////////////////////

  private Boolean trial;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime actualStartDate;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime endDate;

  private Long execBy;

  private int schedulingNum;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastSchedulingDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json")
  private List<RunnerRunVo> lastSchedulingResult;

  /**
   * ========MeterResult=======
   */
  private String meterStatus;

  private String meterMessage;

  private Boolean singleTargetPipeline;

  /**
   * Search for ID
   */
  private String extSearchMerge;

  @Transient
  private ExecSample finishSample;
  @Transient
  private String scriptName;
  @Transient
  private String scriptSourceName;
  @Transient
  private boolean hasOperationPermission;

  public boolean canUpdateTestResult() {
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
