package cloud.xcan.sdf.model.scenario;

import cloud.xcan.sdf.model.script.TestType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioTestCount {

  private long totalScenarioNum;

  private long enabledTestNum;

  private long enabledFuncTestNum;

  private long enabledPerfTestNum;

  private long enabledStabilityTestNum;

  private List<ScenarioInfo> allScenarios;

  private Map<TestType, List<Long>> enabledTestScenarioIds = new HashMap<>();

}
