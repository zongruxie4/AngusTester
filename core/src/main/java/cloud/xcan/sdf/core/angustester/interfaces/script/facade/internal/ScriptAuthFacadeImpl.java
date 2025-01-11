package cloud.xcan.sdf.core.angustester.interfaces.script.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.ScriptAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ScriptAuthFacadeImpl implements ScriptAuthFacade {

  @Resource
  private ScriptAuthCmd scriptAuthCmd;

  @Resource
  private ScriptAuthQuery scriptAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long scriptId, ScriptAuthAddDto dto) {
    return scriptAuthCmd.add(ScriptAuthAssembler.addDtoToDomain(scriptId, dto));
  }

  @Override
  public void replace(Long scriptId, ScriptAuthReplaceDto dto) {
    scriptAuthCmd.replace(ScriptAuthAssembler.replaceDtoToDomain(scriptId, dto));
  }

  @Override
  public void delete(Long scriptId) {
    scriptAuthCmd.delete(scriptId);
  }

  @Override
  public void enabled(Long scriptId, Boolean enabledFlag) {
    scriptAuthCmd.enabled(scriptId, enabledFlag);
  }

  @Override
  public Boolean status(Long scriptId) {
    return scriptAuthQuery.status(scriptId);
  }

  @Override
  public List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean adminFlag) {
    return scriptAuthQuery.userAuth(scriptId, userId, adminFlag);
  }

  @Override
  public ScriptAuthCurrentVo currentUserAuth(Long scriptId, Boolean adminFlag) {
    ScriptAuthCurrent authCurrent = scriptAuthQuery.currentUserAuth(scriptId, adminFlag);
    return ScriptAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public Map<Long, ScriptAuthCurrentVo> currentUserAuths(HashSet<Long> scriptIds,
      Boolean adminFlag) {
    Map<Long, ScriptAuthCurrent> authCurrentsMap = scriptAuthQuery
        .currentUserAuths(scriptIds, adminFlag);
    return ObjectUtils.isEmpty(authCurrentsMap) ? null : authCurrentsMap.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey,
            x -> ScriptAuthAssembler.toAuthCurrentVo(x.getValue())));
  }

  @Override
  public void authCheck(Long scriptId, ScriptPermission authPermission, Long userId) {
    scriptAuthQuery.check(scriptId, authPermission, userId);
  }

  @Override
  @NameJoin
  public PageResult<ScriptAuthVo> list(ScriptAuthFindDto dto) {
    List<String> scriptIds = dto.getFilterInValue("scriptId");
    if (dto.getScriptId() != null) {
      scriptIds.add(String.valueOf(dto.getScriptId()));
    }
    Page<ScriptAuth> dirAuthPage = scriptAuthQuery
        .find(ScriptAuthAssembler.getSpecification(dto), scriptIds, dto.tranPage());
    return buildVoPageResult(dirAuthPage, ScriptAuthAssembler::toDetailVo);
  }

}