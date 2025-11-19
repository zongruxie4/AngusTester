package cloud.xcan.angus.core.tester.application.query.test;

import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import java.util.List;

public interface FuncCaseScenarioQuery {

  List<Scenario> findScenariosByCaseId(Long caseId);

}

