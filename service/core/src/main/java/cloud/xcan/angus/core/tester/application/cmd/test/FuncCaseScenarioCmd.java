package cloud.xcan.angus.core.tester.application.cmd.test;

import java.util.HashSet;

public interface FuncCaseScenarioCmd {

  void scenarioAssocAdd(Long caseId, HashSet<Long> scenarioIds);

  void scenarioAssocCancel(Long caseId, HashSet<Long> scenarioIds);

}

