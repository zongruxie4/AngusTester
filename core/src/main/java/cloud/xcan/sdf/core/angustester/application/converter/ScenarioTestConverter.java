package cloud.xcan.sdf.core.angustester.application.converter;

import static java.util.Objects.nonNull;

import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.model.scenario.ScenarioTestCount;
import cloud.xcan.sdf.core.angustester.domain.kanban.TestScenarioCount;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import java.util.List;
import java.util.stream.Collectors;

public class ScenarioTestConverter {

  public static void assembleScenarioTestCount(ScenarioTestCount count, List<Scenario> scenarios) {
    List<Long> enabledFuncScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestFuncFlag()) && x.getTestFuncFlag())
        .map(Scenario::getId).collect(Collectors.toList());
    count.setEnabledFuncTestNum(enabledFuncScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.FUNCTIONAL, enabledFuncScenarioId);

    List<Long> enabledPerfScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestPerfFlag()) && x.getTestPerfFlag())
        .map(Scenario::getId).collect(Collectors.toList());
    count.setEnabledPerfTestNum(enabledPerfScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.PERFORMANCE, enabledPerfScenarioId);

    List<Long> enabledStabilityScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestStabilityFlag()) && x.getTestStabilityFlag())
        .map(Scenario::getId).collect(Collectors.toList());
    count.setEnabledStabilityTestNum(enabledStabilityScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.STABILITY, enabledStabilityScenarioId);

    count.setTotalScenarioNum(scenarios.size());
    count.setEnabledTestNum(count.getEnabledFuncTestNum() + count.getEnabledPerfTestNum()
        + count.getEnabledStabilityTestNum());
    count.setAllScenarios(scenarios.stream().map(ScenarioConverter::toScenarioInfo)
        .collect(Collectors.toList()));
  }

  public static void assembleTestScenarioCount(TestScenarioCount count, List<Scenario> scenarios) {
    count.setTotalScenarioNum(scenarios.size());
    count.setEnabledTestScenarioNum(scenarios.stream().filter(Scenario::isEnabledTest).count());
    count.setEnabledTestScenarioRate(count.calcEnabledTestScenarioRate());
    count.setPassedTestScenarioNum(scenarios.stream().filter(Scenario::isPassedTest).count());
    count.setTestScenarioProgress(count.calcTestScenarioProgress());

    count.setEnabledTestNum(scenarios.stream()
        .map(Scenario::getEnabledTestNum).reduce(0, Integer::sum));
    count.setPassedTestNum(scenarios.stream()
        .map(Scenario::getPassedTestNum).reduce(0, Integer::sum));
    count.setTestProgress(count.calcTestProgress());
  }

}
