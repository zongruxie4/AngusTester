package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.ScenarioAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.auth.ScenarioAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.internal.assembler.ScenarioAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.vo.auth.ScenarioAuthVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import javax.annotation.Resource;
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
  public void enabled(Long scenarioId, Boolean enabledFlag) {
    scenarioAuthCmd.enabled(scenarioId, enabledFlag);
  }

  @Override
  public Boolean status(Long scenarioId) {
    return scenarioAuthQuery.status(scenarioId);
  }

  @Override
  public List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean adminFlag) {
    return scenarioAuthQuery.userAuth(scenarioId, userId, adminFlag);
  }

  @Override
  public ScenarioAuthCurrentVo currentUserAuth(Long scenarioId, Boolean adminFlag) {
    ScenarioAuthCurrent authCurrent = scenarioAuthQuery.currentUserAuth(scenarioId, adminFlag);
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




