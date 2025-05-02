package cloud.xcan.angus.core.tester.interfaces.exec.facade;

import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByMonitorDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScenarioDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartByScriptDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.dto.debug.ExecDebugStartDto;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug.ExecDebugDetailVo;

public interface ExecDebugFacade {

  ExecDebugDetailVo start(ExecDebugStartDto dto);

  ExecDebugDetailVo startByScript(ExecDebugStartByScriptDto dto);

  ExecDebugDetailVo startByScenario(ExecDebugStartByScenarioDto dto);

  ExecDebugDetailVo startByMonitor(ExecDebugStartByMonitorDto dto);

  ExecDebugDetailVo scriptDetail(Long id);

  ExecDebugDetailVo scenarioDetail(Long id);

  ExecDebugDetailVo monitorDetail(Long id);
}
