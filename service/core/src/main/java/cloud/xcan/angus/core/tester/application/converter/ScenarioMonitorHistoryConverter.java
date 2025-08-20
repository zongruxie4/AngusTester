package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.agent.AgentConstant.DEFAULT_SERVER_PORT;
import static cloud.xcan.angus.core.utils.HttpSenderUtils.HTTP_SENDER;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.pojo.node.NodeInfo;
import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorHistory;
import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.spec.http.HttpSender.Request;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import java.util.stream.Collectors;

public class ScenarioMonitorHistoryConverter {

  public static void assembleScenarioMonitorResultInfo(ScenarioMonitorHistory history,
      ExecDebug debug) {
    history.setStatus(nullSafe(debug.getSucceed(), false) ? ScenarioMonitorStatus.SUCCESS
            : ScenarioMonitorStatus.FAILURE)
        .setFailureMessage(debug.getFailureMessage())
        .setResponseDelay(nonNull(debug.getFinishSampleResult())
            ? debug.getFinishSampleResult().getDuration() : null)
        .setExecId(debug.getId())
        .setExecStartDate(debug.getCreatedDate()).setExecEndDate(debug.getEndDate())
        .setSampleContents(debug.getSampleContents().stream()
            .map(ExecSampleConverter::toExecSampleContentInfo).toList())
        .setSchedulingResult(debug.getSchedulingResult());
  }

  public static void readExecutionLogFromRemote(
      ExecDebug result, ScenarioMonitorHistory history) throws Throwable {
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
