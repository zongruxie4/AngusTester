package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.angus.agent.AgentConstant.DEFAULT_SERVER_PORT;
import static cloud.xcan.angus.core.utils.HttpSenderUtils.HTTP_SENDER;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.dto.debug.ExecDebugStartByMonitorDto;
import cloud.xcan.sdf.api.angusctrl.exec.vo.debug.ExecDebugDetailVo;
import cloud.xcan.sdf.api.pojo.node.NodeInfo;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitor;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.sdf.spec.http.HttpSender.Request;
import cloud.xcan.sdf.spec.http.HttpSender.Response;

public class ScenarioMonitorHistoryConverter {

  public static ExecDebugStartByMonitorDto assembleExecDebugStartByMonitorDto(
      ScenarioMonitor monitor) {
    return new ExecDebugStartByMonitorDto()
        .setBroadcast(true)
        .setScenarioId(monitor.getScenarioId())
        .setScriptId(monitor.getScriptId())
        .setMonitorId(monitor.getId())
        .setScriptType(ScriptType.TEST_FUNCTIONALITY)
        .setServerSetting(monitor.getServerSetting());
  }

  public static void assembleScenarioMonitorResultInfo(ScenarioMonitorHistory history,
      ExecDebugDetailVo result) {
    history.setStatus(result.isTestSucceed() ? ScenarioMonitorStatus.SUCCESS
            : ScenarioMonitorStatus.FAILURE)
        .setFailureMessage(result.getTestFailureMessage())
        .setResponseDelay(nonNull(result.getSampleSummaryInfo())
            ? result.getSampleSummaryInfo().getDuration() : null)
        .setExecId(result.getId())
        .setExecStartDate(result.getCreatedDate()).setExecEndDate(result.getEndDate())
        .setSampleContents(result.getSampleContents())
        .setSchedulingResult(result.getSchedulingResult());
  }

  public static void readExecutionLogFromRemote(
      ExecDebugDetailVo result, ScenarioMonitorHistory history) throws Throwable {
    NodeInfo execNode = result.getExecNode();
    if (nonNull(execNode)) {
      Response response = Request.build(String.format("http://%s:%s/actuator/runner/log/%s",
          stringSafe(execNode.getPublicIp(), execNode.getIp()),
          nullSafe(execNode.getAgentPort(), DEFAULT_SERVER_PORT),
          result.getId()), HTTP_SENDER).send();
      history.setSampleLogContent(response.body());
    }
  }

}
