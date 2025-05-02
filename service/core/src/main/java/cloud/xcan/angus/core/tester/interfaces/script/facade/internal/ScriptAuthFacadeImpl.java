package cloud.xcan.angus.core.tester.interfaces.script.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler.toAuthCurrentVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.angus.core.tester.application.query.script.ScriptAuthQuery;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuth;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuthCurrent;
import cloud.xcan.angus.core.tester.interfaces.script.facade.ScriptAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.internal.assembler.ScriptAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
    return scriptAuthCmd.add(addDtoToDomain(scriptId, dto));
  }

  @Override
  public void replace(Long scriptId, ScriptAuthReplaceDto dto) {
    scriptAuthCmd.replace(replaceDtoToDomain(scriptId, dto));
  }

  @Override
  public void delete(Long scriptId) {
    scriptAuthCmd.delete(scriptId);
  }

  @Override
  public void enabled(Long scriptId, Boolean enabled) {
    scriptAuthCmd.enabled(scriptId, enabled);
  }

  @Override
  public Boolean status(Long scriptId) {
    return scriptAuthQuery.status(scriptId);
  }

  @Override
  public List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean admin) {
    return scriptAuthQuery.userAuth(scriptId, userId, admin);
  }

  @Override
  public ScriptAuthCurrentVo currentUserAuth(Long scriptId, Boolean admin) {
    ScriptAuthCurrent authCurrent = scriptAuthQuery.currentUserAuth(scriptId, admin);
    return toAuthCurrentVo(authCurrent);
  }

  @Override
  public Map<Long, ScriptAuthCurrentVo> currentUserAuths(HashSet<Long> scriptIds,
      Boolean admin) {
    Map<Long, ScriptAuthCurrent> authMap = scriptAuthQuery.currentUserAuths(scriptIds, admin);
    return isEmpty(authMap) ? null : authMap.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey, x -> toAuthCurrentVo(x.getValue())));
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
    Page<ScriptAuth> page = scriptAuthQuery.find(getSpecification(dto), scriptIds, dto.tranPage());
    return buildVoPageResult(page, ScriptAuthAssembler::toDetailVo);
  }

}
