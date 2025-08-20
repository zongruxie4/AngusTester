package cloud.xcan.angus.core.tester.application.converter;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.tester.domain.kanban.TestScenarioCount;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.model.scenario.ScenarioTestCount;
import cloud.xcan.angus.model.script.TestType;
import java.util.List;
import java.util.stream.Collectors;

public class ScenarioTestConverter {

  public static void assembleScenarioTestCount(ScenarioTestCount count, List<Scenario> scenarios) {
    List<Long> enabledFuncScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestFunc()) && x.getTestFunc())
        .map(Scenario::getId).toList();
    count.setEnabledFuncTestNum(enabledFuncScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.FUNCTIONAL, enabledFuncScenarioId);

    List<Long> enabledPerfScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestPerf()) && x.getTestPerf())
        .map(Scenario::getId).toList();
    count.setEnabledPerfTestNum(enabledPerfScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.PERFORMANCE, enabledPerfScenarioId);

    List<Long> enabledStabilityScenarioId = scenarios.stream()
        .filter(x -> nonNull(x.getTestStability()) && x.getTestStability())
        .map(Scenario::getId).toList();
    count.setEnabledStabilityTestNum(enabledStabilityScenarioId.size());
    count.getEnabledTestScenarioIds().put(TestType.STABILITY, enabledStabilityScenarioId);

    count.setTotalScenarioNum(scenarios.size());
    count.setEnabledTestNum(count.getEnabledFuncTestNum() + count.getEnabledPerfTestNum()
        + count.getEnabledStabilityTestNum());
    count.setAllScenarios(scenarios.stream().map(ScenarioConverter::toScenarioInfo)
        .toList());
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
