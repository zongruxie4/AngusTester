package cloud.xcan.angus.core.tester.application.cmd.exec;

import cloud.xcan.angus.core.tester.domain.exec.debug.ExecDebug;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

public interface ExecDebugCmd {

  String EXEC_DEBUG_ENDPOINT_PREFIX = "/api/v1/exec/debug";
  String EXEC_DEBUG_START_ENDPOINT = EXEC_DEBUG_ENDPOINT_PREFIX + "/start";

  ExecDebug start(boolean broadcast, ExecDebug debug);

  ExecDebug startByScript(boolean broadcast, Long id, Long scriptId, ScriptType scriptType,
      Configuration configuration, Arguments arguments);

  ExecDebug startByScenario(boolean broadcast, Long id, Long scenarioId, Long scriptId,
      ScriptType scriptType, Configuration configuration, Arguments arguments);

  ExecDebug startByMonitor(boolean broadcast, Long id, Long monitorId, Long scenarioId,
      Long scriptId, ScriptType scriptType, Configuration configuration, Arguments arguments,
      List<Server> serverSetting);

}
