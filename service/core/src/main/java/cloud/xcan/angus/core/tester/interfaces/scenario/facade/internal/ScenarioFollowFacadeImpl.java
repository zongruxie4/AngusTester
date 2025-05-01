package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioFollowAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioFollowCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioFollowQuery;
import cloud.xcan.angus.core.tester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioFollowFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioFollowAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioFollowFacadeImpl implements ScenarioFollowFacade {

  @Resource
  private ScenarioFollowCmd scenarioFollowCmd;

  @Resource
  private ScenarioFollowQuery scenarioFollowQuery;

  @Override
  public IdKey<Long, Object> add(Long scenarioId) {
    return scenarioFollowCmd.add(addDtoToDomain(scenarioId));
  }

  @Override
  public void cancel(Long scenarioId) {
    scenarioFollowCmd.cancel(scenarioId);
  }

  @Override
  public void cancelAll(Long projectId) {
    scenarioFollowCmd.cancelAll(projectId);
  }

  @Override
  public PageResult<ScenarioFollowDetailVo> search(ScenarioFollowFindDto dto) {
    Page<ScenarioFollow> page = scenarioFollowQuery
        .search(dto.getProjectId(), dto.getScenarioName(), dto.tranPage());
    return buildVoPageResult(page, ScenarioFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return scenarioFollowQuery.count(projectId);
  }

}

