package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.ScenarioListVo;
import java.util.HashSet;
import java.util.List;

public interface FuncCaseScenarioFacade {

  void scenarioAssocAdd(Long caseId, HashSet<Long> scenarioIds);

  void scenarioAssocCancel(Long caseId, HashSet<Long> scenarioIds);

  List<ScenarioListVo> listScenarios(Long caseId);

}

