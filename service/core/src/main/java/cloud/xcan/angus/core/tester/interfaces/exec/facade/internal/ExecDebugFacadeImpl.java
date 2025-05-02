package cloud.xcan.angus.core.tester.interfaces.exec.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecDebugAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.exec.facade.internal.assembler.ExecDebugAssembler.toDetailVo;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecDebugCmd;
import cloud.xcan.angus.core.tester.application.query.exec.ExecDebugQuery;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecDebugFacade;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByMonitorDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScenarioDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExecDebugFacadeImpl implements ExecDebugFacade {

  @Resource
  private ExecDebugCmd execDebugCmd;

  @Resource
  private ExecDebugQuery execDebugQuery;

  @Override
  public ExecDebugDetailVo start(ExecDebugStartDto dto) {
    return toDetailVo(execDebugCmd.start(dto.isBroadcast(), addDtoToDomain(dto)));
  }

  @Override
  public ExecDebugDetailVo startByScript(ExecDebugStartByScriptDto dto) {
    return toDetailVo(execDebugCmd.startByScript(dto.isBroadcast(), dto.getId(), dto.getScriptId(),
        dto.getScriptType(), dto.getConfiguration(), dto.getArguments()));
  }

  @Override
  public ExecDebugDetailVo startByScenario(ExecDebugStartByScenarioDto dto) {
    return toDetailVo(execDebugCmd.startByScenario(dto.isBroadcast(), dto.getId(),
        dto.getScenarioId(), dto.getScriptId(), dto.getScriptType(), dto.getConfiguration(),
        dto.getArguments()));
  }

  @Override
  public ExecDebugDetailVo startByMonitor(ExecDebugStartByMonitorDto dto) {
    return toDetailVo(execDebugCmd.startByMonitor(dto.isBroadcast(), dto.getId(),
        dto.getMonitorId(), dto.getScenarioId(), dto.getScriptId(), dto.getScriptType(),
        dto.getConfiguration(), dto.getArguments(), dto.getServerSetting()));
  }

  @NameJoin
  @Override
  public ExecDebugDetailVo scenarioDetail(Long id) {
    return toDetailVo(execDebugQuery.scenarioDetail(id));
  }

  @NameJoin
  @Override
  public ExecDebugDetailVo scriptDetail(Long id) {
    return toDetailVo(execDebugQuery.scriptDetail(id));
  }

  @Override
  public ExecDebugDetailVo monitorDetail(Long id) {
    return toDetailVo(execDebugQuery.monitorDetail(id));
  }

}
