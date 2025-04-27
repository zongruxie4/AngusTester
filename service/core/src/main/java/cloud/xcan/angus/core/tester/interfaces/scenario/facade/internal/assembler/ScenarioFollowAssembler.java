package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler;

import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;


public class ScenarioFollowAssembler {

  public static ScenarioFollow addDtoToDomain(Long scenarioId) {
    return new ScenarioFollow().setScenarioId(scenarioId);
  }

  public static ScenarioFollowDetailVo toDetailVo(ScenarioFollow follow) {
    return new ScenarioFollowDetailVo().setId(follow.getId())
        .setScenarioId(follow.getScenarioId()).setScenarioName(follow.getScenarioName())
        .setPlugin(follow.getPlugin());
  }

}




