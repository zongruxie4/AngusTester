package cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioAuthCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.ScenarioAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.internal.assembler.ScenarioAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScenarioAuthFacadeImpl implements ScenarioAuthFacade {

  @Resource
  private ScenarioAuthCmd scenarioAuthCmd;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long scenarioId, ScenarioAuthAddDto dto) {
    return scenarioAuthCmd.add(ScenarioAuthAssembler.addDtoToDomain(scenarioId, dto));
  }

  @Override
  public void replace(Long scenarioId, ScenarioAuthReplaceDto dto) {
    scenarioAuthCmd.replace(ScenarioAuthAssembler.replaceDtoToDomain(scenarioId, dto));
  }

  @Override
  public void delete(Long scenarioId) {
    scenarioAuthCmd.delete(scenarioId);
  }

  @Override
  public void enabled(Long scenarioId, Boolean enabled) {
    scenarioAuthCmd.enabled(scenarioId, enabled);
  }

  @Override
  public Boolean status(Long scenarioId) {
    return scenarioAuthQuery.status(scenarioId);
  }

  @Override
  public List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean admin) {
    return scenarioAuthQuery.userAuth(scenarioId, userId, admin);
  }

  @Override
  public ScenarioAuthCurrentVo currentUserAuth(Long scenarioId, Boolean admin) {
    ScenarioAuthCurrent authCurrent = scenarioAuthQuery.currentUserAuth(scenarioId, admin);
    return ScenarioAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public void authCheck(Long scenarioId, ScenarioPermission authPermission, Long userId) {
    scenarioAuthQuery.check(scenarioId, authPermission, userId);
  }

  @Override
  @NameJoin
  public PageResult<ScenarioAuthVo> list(ScenarioAuthFindDto dto) {
    List<String> scenarioIds = dto.getFilterInValue("scenarioId");
    if (dto.getScenarioId() != null) {
      scenarioIds.add(String.valueOf(dto.getScenarioId()));
    }
    Page<ScenarioAuth> dirAuthPage = scenarioAuthQuery
        .find(ScenarioAuthAssembler.getSpecification(dto), scenarioIds, dto.tranPage());
    return buildVoPageResult(dirAuthPage, ScenarioAuthAssembler::toDetailVo);
  }

}




