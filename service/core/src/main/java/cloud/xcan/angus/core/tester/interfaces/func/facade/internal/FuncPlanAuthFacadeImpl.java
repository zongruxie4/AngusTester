package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncPlanAuthCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncPlanAuthQuery;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncPlanAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncPlanAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.auth.FuncPlanAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncPlanAuthFacadeImpl implements FuncPlanAuthFacade {

  @Resource
  private FuncPlanAuthCmd funcPlanAuthCmd;

  @Resource
  private FuncPlanAuthQuery funcPlanAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long planId, FuncPlanAuthAddDto dto) {
    return funcPlanAuthCmd.add(FuncPlanAuthAssembler.addDtoToDomain(planId, dto));
  }

  @Override
  public void replace(Long planId, FuncPlanAuthReplaceDto dto) {
    funcPlanAuthCmd.replace(FuncPlanAuthAssembler.replaceDtoToDomain(planId, dto));
  }

  @Override
  public void enabled(Long planId, Boolean enabled) {
    funcPlanAuthCmd.enabled(planId, enabled);
  }

  @Override
  public Boolean status(Long planId) {
    return funcPlanAuthQuery.status(planId);
  }

  @Override
  public void delete(Long id) {
    funcPlanAuthCmd.delete(id);
  }

  @Override
  public List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean admin) {
    return funcPlanAuthQuery.userAuth(planId, userId, admin);
  }

  @Override
  public FuncPlanAuthCurrentVo currentUserAuth(Long planId, Boolean admin) {
    FuncPlanAuthCurrent authCurrent = funcPlanAuthQuery.currentUserAuth(planId, admin);
    return FuncPlanAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public Map<Long, FuncPlanAuthCurrentVo> currentUserAuths(HashSet<Long> planIds,
      Boolean admin) {
    Map<Long, FuncPlanAuthCurrent> authCurrentsMap = funcPlanAuthQuery
        .currentUserAuths(planIds, admin);
    return ObjectUtils.isEmpty(authCurrentsMap) ? null : authCurrentsMap.entrySet()
        .stream().collect(Collectors.toMap(Entry::getKey,
            x -> FuncPlanAuthAssembler.toAuthCurrentVo(x.getValue())));
  }

  @Override
  public void authCheck(Long planId, FuncPlanPermission permission, Long userId) {
    funcPlanAuthQuery.check(planId, permission, userId);
  }

  @NameJoin
  @Override
  public PageResult<FuncPlanAuthVo> list(FuncPlanAuthFindDto dto) {
    List<String> planIds = dto.getFilterInValue("planId");
    if (dto.getPlanId() != null) {
      planIds.add(String.valueOf(dto.getPlanId()));
    }
    Page<FuncPlanAuth> dirAuthPage = funcPlanAuthQuery
        .find(FuncPlanAuthAssembler.getSpecification(dto), planIds, dto.tranPage());
    return buildVoPageResult(dirAuthPage, FuncPlanAuthAssembler::toDetailVo);
  }

}
