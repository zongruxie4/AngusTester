package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioFollowQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.follow.ScenarioFollow;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioFollowFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.follow.ScenarioFollowFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioFollowAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.follow.ScenarioFollowDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
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
    return scenarioFollowCmd.add(ScenarioFollowAssembler.addDtoToDomain(scenarioId));
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
    Page<ScenarioFollow> pageResult = scenarioFollowQuery
        .search(dto.getProjectId(), dto.getScenarioName(), dto.tranPage());
    return buildVoPageResult(pageResult, ScenarioFollowAssembler::toDetailVo);
  }

  @Override
  public Long count(Long projectId) {
    return scenarioFollowQuery.count(projectId);
  }

}

