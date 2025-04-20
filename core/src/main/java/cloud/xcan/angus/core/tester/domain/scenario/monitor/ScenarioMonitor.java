package cloud.xcan.angus.core.tester.domain.scenario.monitor;

import cloud.xcan.angus.api.enums.CreatedAt;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioMonitorCount;
import cloud.xcan.angus.core.tester.domain.setting.MonitorTimeSetting;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.models.servers.Server;
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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "scenario_monitor")
@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class ScenarioMonitor extends TenantAuditingEntity<ScenarioMonitor, Long>
    implements ActivityResource {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "scenario_id")
  private Long scenarioId;

  @Column(name = "script_id")
  private Long scriptId;

  private String name;

  private String description;

  @Enumerated(EnumType.STRING)
  private ScenarioMonitorStatus status;

  @Column(name = "failure_message")
  private String failureMessage;

  @Column(name = "next_exec_date")
  private LocalDateTime nextExecDate;

  @Column(name = "last_monitor_history_id")
  private Long lastMonitorHistoryId;

  @Column(name = "last_monitor_date")
  private LocalDateTime lastMonitorDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "created_at")
  private CreatedAt createdAt;

  @Type(JsonType.class)
  @Column(name = "time_setting")
  private MonitorTimeSetting timeSetting;

  @Type(JsonType.class)
  @Column(name = "server_setting")
  private List<Server> serverSetting;

  @Type(JsonType.class)
  @Column(name = "notice_setting")
  private NoticeSetting noticeSetting;

  @Transient
  private ScenarioMonitorCount count;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return id;
  }
}
