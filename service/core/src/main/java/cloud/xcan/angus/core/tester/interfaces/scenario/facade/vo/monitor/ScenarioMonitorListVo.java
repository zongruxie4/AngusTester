package cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.angus.core.tester.domain.scenario.count.ScenarioMonitorCount;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorListVo {

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

  private ScenarioMonitorCount count;

  private Long lastMonitorHistoryId;

  private LocalDateTime lastMonitorDate;

  //private MonitorTimeSetting timeSetting;

  //private List<Server> serverSetting;

  //private NoticeSetting noticeSetting;

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
