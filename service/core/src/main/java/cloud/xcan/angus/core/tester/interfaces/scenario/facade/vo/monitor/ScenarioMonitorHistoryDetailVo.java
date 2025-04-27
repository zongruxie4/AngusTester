package cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.monitor;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.ctrl.exec.vo.ExecSampleContentVo;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


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
