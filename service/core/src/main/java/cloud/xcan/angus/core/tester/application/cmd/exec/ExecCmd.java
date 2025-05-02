package cloud.xcan.angus.core.tester.application.cmd.exec;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.agent.message.runner.RunnerStopVo;
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.ExecStopDto;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.unit.TimeValue;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;

public interface ExecCmd {

  String EXEC_ENDPOINT_PREFIX = "/innerapi/v1/exec";
  String EXEC_ADD_BY_SCRIPT_ENDPOINT = EXEC_ENDPOINT_PREFIX + "/byscript";
  String EXEC_START_ENDPOINT = EXEC_ENDPOINT_PREFIX + "/start";
  String EXEC_STOP_ENDPOINT = EXEC_ENDPOINT_PREFIX + "/stop";

  IdKey<Long, Object> addByLocalScriptContent(Long projectId, String name, String scriptContent,
      Boolean trial);

  IdKey<Long, Object> addByLocalScriptArgs(Long projectId, String name, ScriptType scriptType,
      String plugin, Configuration configuration, Task task, Boolean trial);

  IdKey<Long, Object> addByLocalScript(Long projectId, String name, Long scriptId,
      String scriptContent, AngusScript angusScript, Boolean trial);

  IdKey<Long, Object> addByRemoteScript(String name, Long scriptId, ScriptType scriptType,
      Configuration configuration, Arguments arguments, Boolean trial);

  IdKey<Long, Object> add0(Exec exec);

  void configReplace(Long id, String name, Long iterations, TimeValue duration, Integer thread,
      Integer priority, Boolean ignoreAssertions, Boolean updateTestResult, StartMode startMode,
      LocalDateTime startDate, TimeValue timeout);

  void scriptConfigReplace(Long id, String name, ScriptType scriptType, Configuration configuration,
      Arguments arguments);

  List<RunnerRunVo> start(ExecStartDto dto);

  List<RunnerRunVo> start0(Exec exec, ExecStartDto dto);

  List<RunnerStopVo> stop(ExecStopDto dto);

  List<RunnerStopVo> stop0(ExecInfo exec, ExecStopDto dto);

  void delete(LinkedHashSet<Long> ids);

}
