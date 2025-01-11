package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecSampleContentVo;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.spec.experimental.EntitySupport;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "scenario_monitor_history")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "sample_content")
  private List<ExecSampleContentVo> sampleContents;

  @Column(name = "sample_log_content")
  private String sampleLogContent;

  @Type(type = "json")
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
