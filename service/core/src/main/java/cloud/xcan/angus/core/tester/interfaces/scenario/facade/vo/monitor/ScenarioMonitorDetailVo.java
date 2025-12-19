package cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioMonitorCount;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.core.tester.domain.setting.MonitorTimeSetting;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.models.servers.Server;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorDetailVo {

  private Long id;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private Long scenarioId;

  @NameJoinField(id = "scenarioId", repository = "scenarioRepo")
  private String scenarioName;

  private String name;

  private String description;

  private ScenarioMonitorStatus status;

  private String failureMessage;

  private LocalDateTime nextExecDate;

  private Long lastMonitorHistoryId;

  private LocalDateTime lastMonitorDate;

  private MonitorTimeSetting timeSetting;

  private List<Server> serverSetting;

  private NoticeSetting noticeSetting;

  private ScenarioMonitorCount count;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

  private Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;

}
