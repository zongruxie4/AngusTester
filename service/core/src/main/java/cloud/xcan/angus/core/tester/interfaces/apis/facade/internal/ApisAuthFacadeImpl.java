package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler.toAuthCurrentVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthCurrent;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ApisAuthFacadeImpl implements ApisAuthFacade {

  @Resource
  private ApisAuthCmd apisAuthCmd;

  @Resource
  private ApisAuthQuery apisAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long apiId, ApisAuthAddDto dto) {
    return apisAuthCmd.add(addDtoToDomain(apiId, dto));
  }

  @Override
  public void replace(Long id, ApisAuthReplaceDto dto) {
    apisAuthCmd.replace(replaceDtoToDomain(id, dto));
  }

  @Override
  public void delete(Long id) {
    apisAuthCmd.delete(id);
  }

  @Override
  public void enabled(Long projectId, Boolean enabled) {
    apisAuthCmd.enabled(projectId, enabled);
  }

  @Override
  public Boolean status(Long apiId) {
    return apisAuthQuery.status(apiId);
  }

  @Override
  public List<ApiPermission> userAuth(Long apiId, Long userId, Boolean admin) {
    return apisAuthQuery.userAuth(apiId, userId, admin);
  }

  @Override
  public ApisAuthCurrentVo currentUserAuth(Long apiId, Boolean admin) {
    ApisAuthCurrent authCurrent = apisAuthQuery.currentUserAuth(apiId, admin);
    return toAuthCurrentVo(authCurrent);
  }

  @Override
  public void authCheck(Long apiId, ApiPermission permission, Long userId) {
    apisAuthQuery.check(apiId, permission, userId);
  }

  @NameJoin
  @Override
  public PageResult<ApiAuthVo> list(ApisAuthFindDto dto) {
    List<String> apiIds = dto.getFilterInValue("apisId");
    if (dto.getApisId() != null) {
      apiIds.add(String.valueOf(dto.getApisId()));
    }
    Page<ApisAuth> page = apisAuthQuery.find(apiIds, getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, ApisAuthAssembler::toDetailVo);
  }

}
