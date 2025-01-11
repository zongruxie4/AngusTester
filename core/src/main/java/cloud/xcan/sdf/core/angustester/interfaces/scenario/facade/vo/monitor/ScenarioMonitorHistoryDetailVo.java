package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.angusctrl.exec.vo.ExecSampleContentVo;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorHistoryDetailVo {

  private Long id;

  private Long projectId;

  private Long monitorId;

  private ScenarioMonitorStatus status;

  private String failureMessage;

  private Long responseDelay;

  private Long execId;

  private LocalDateTime execStartDate;

  private LocalDateTime execEndDate;

  private RunnerRunVo schedulingResult;

  private List<ExecSampleContentVo> sampleContents;

  private String sampleLogContent;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

}
