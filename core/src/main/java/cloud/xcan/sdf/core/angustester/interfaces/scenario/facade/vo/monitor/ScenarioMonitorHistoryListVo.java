package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.sdf.api.NameJoinField;
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
public class ScenarioMonitorHistoryListVo {

  private Long id;

  private Long projectId;

  private Long monitorId;

  private ScenarioMonitorStatus status;

  private String failureMessage;

  private Long responseDelay;

  private Long execId;

  private LocalDateTime execStartDate;

  private LocalDateTime execEndDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

}
