package cloud.xcan.angus.core.tester.domain.scenario.monitor;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecSampleContentInfo;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "scenario_monitor_history")
@EntityListeners({AuditingEntityListener.class})
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ScenarioMonitorHistory extends EntitySupport<ScenarioMonitorHistory, Long> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "monitor_id")
  private Long monitorId;

  @Enumerated(EnumType.STRING)
  private ScenarioMonitorStatus status;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "response_delay")
  private Long responseDelay;

  @Column(name = "exec_id")
  private Long execId;

  @Column(name = "exec_start_date")
  private LocalDateTime execStartDate;

  @Column(name = "exec_end_date")
  private LocalDateTime execEndDate;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "sample_content")
  private List<ExecSampleContentInfo> sampleContents;

  @Column(name = "sample_log_content")
  private String sampleLogContent;

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "scheduling_result")
  private RunnerRunVo schedulingResult;

  @CreatedDate
  @DateTimeFormat(pattern = DATE_FMT)
  @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
  protected LocalDateTime createdDate;

  @CreatedBy
  @Column(name = "created_by", nullable = false, updatable = false)
  protected Long createdBy;

  @Override
  public Long identity() {
    return id;
  }
}
