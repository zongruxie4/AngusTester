package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseScenarioCmd;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseScenarioQuery;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncCaseScenarioFacade;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FuncCaseScenarioFacadeImpl implements FuncCaseScenarioFacade {

  @Resource
  private FuncCaseScenarioCmd funcCaseScenarioCmd;

  @Resource
  private FuncCaseScenarioQuery funcCaseScenarioQuery;

  @Override
  public void scenarioAssocAdd(Long caseId, HashSet<Long> scenarioIds) {
    funcCaseScenarioCmd.scenarioAssocAdd(caseId, scenarioIds);
  }

  @Override
  public void scenarioAssocCancel(Long caseId, HashSet<Long> scenarioIds) {
    funcCaseScenarioCmd.scenarioAssocCancel(caseId, scenarioIds);
  }

  @NameJoin
  @Override
  public List<ScenarioListVo> listScenarios(Long caseId) {
    return funcCaseScenarioQuery.findScenariosByCaseId(caseId).stream()
        .map(ScenarioAssembler::toListVo).toList();
  }
}

