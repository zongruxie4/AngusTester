package cloud.xcan.sdf.core.angustester.domain.scenario.monitor;

import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioMonitorCount;
import cloud.xcan.sdf.core.angustester.domain.setting.NoticeSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import cloud.xcan.sdf.core.jpa.hibernate.type.json.JsonStringType;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@Entity
@Table(name = "scenario_monitor")
@TypeDef(name = "json", typeClass = JsonStringType.class)
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

  @Type(type = "json")
  @Column(name = "time_setting")
  private TimeSetting timeSetting;

  @Type(type = "json")
  @Column(name = "server_setting")
  private List<Server> serverSetting;

  @Type(type = "json")
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
