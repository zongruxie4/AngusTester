package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuth;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuthCurrent;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.ApisAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.internal.assembler.ApisAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import javax.annotation.Resource;
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
    return apisAuthCmd.add(ApisAuthAssembler.addDtoToDomain(apiId, dto));
  }

  @Override
  public void replace(Long id, ApisAuthReplaceDto dto) {
    apisAuthCmd.replace(ApisAuthAssembler.replaceDtoToDomain(id, dto));
  }

  @Override
  public void delete(Long id) {
    apisAuthCmd.delete(id);
  }

  @Override
  public void enabled(Long projectId, Boolean enabledFlag) {
    apisAuthCmd.enabled(projectId, enabledFlag);
  }

  @Override
  public Boolean status(Long apiId) {
    return apisAuthQuery.status(apiId);
  }

  @Override
  public List<ApiPermission> userAuth(Long apiId, Long userId, Boolean adminFlag) {
    return apisAuthQuery.userAuth(apiId, userId, adminFlag);
  }

  @Override
  public ApisAuthCurrentVo currentUserAuth(Long apiId, Boolean adminFlag) {
    ApisAuthCurrent authCurrent = apisAuthQuery.currentUserAuth(apiId, adminFlag);
    return ApisAuthAssembler.toAuthCurrentVo(authCurrent);
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
    Page<ApisAuth> apisAuthPage = apisAuthQuery
        .find(apiIds, ApisAuthAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(apisAuthPage, ApisAuthAssembler::toDetailVo);
  }

}
