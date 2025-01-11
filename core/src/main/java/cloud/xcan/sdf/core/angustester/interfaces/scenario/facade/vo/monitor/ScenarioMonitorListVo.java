package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.scenario.count.ScenarioMonitorCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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

  //private TimeSetting timeSetting;

  //private List<Server> serverSetting;

  //private NoticeSetting noticeSetting;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
