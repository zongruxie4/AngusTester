package cloud.xcan.angus.core.tester.interfaces.services.facade.internal;

import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesAuthCmd;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthCurrent;
import cloud.xcan.angus.core.tester.interfaces.services.facade.ServicesAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.internal.assembler.ServicesAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ServicesAuthFacadeImpl implements ServicesAuthFacade {

  @Resource
  private ServicesAuthCmd servicesAuthCmd;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long serviceId, ServicesAddAuthDto dto) {
    return servicesAuthCmd.add(ServicesAuthAssembler.addDtoToDomain(serviceId, dto));
  }

  @Override
  public void replace(Long id, ServicesAuthReplaceDto dto) {
    servicesAuthCmd.replace(ServicesAuthAssembler.replaceDtoToDomain(id, dto));
  }

  @Override
  public void delete(Long id) {
    servicesAuthCmd.delete(id);
  }

  @Override
  public void enabled(Long serviceId, Boolean enabled) {
    servicesAuthCmd.enabled(serviceId, enabled);
  }

  @Override
  public Boolean status(Long serviceId) {
    return servicesAuthQuery.status(serviceId);
  }

  @Override
  public void apisEnabled(Long serviceId, Boolean enabled) {
    servicesAuthCmd.apisEnabled(serviceId, enabled);
  }

  @Override
  public List<ServicesPermission> userAuth(Long serviceId, Long userId, Boolean admin) {
    return servicesAuthQuery.userAuth(serviceId, userId, admin);
  }

  @Override
  public ServiceAuthCurrentVo currentUserAuth(Long serviceId, Boolean admin) {
    ServicesAuthCurrent authCurrent = servicesAuthQuery.currentUserAuth(serviceId, admin);
    return ServicesAuthAssembler.toAuthCurrentVo(authCurrent);
  }

  @Override
  public void authCheck(Long serviceId, ServicesPermission authPermission, Long userId) {
    servicesAuthQuery.check(serviceId, authPermission, userId);
  }

  @Override
  @NameJoin
  public PageResult<ServicesAuthVo> list(ServicesAuthFindDto dto) {
    List<String> projectIds = dto.getFilterInValue("serviceId");
    if (dto.getServiceId() != null) {
      projectIds.add(String.valueOf(dto.getServiceId()));
    }
    Page<ServicesAuth> apisAuthPage = servicesAuthQuery
        .find(ServicesAuthAssembler.getSpecification(dto), projectIds, dto.tranPage());
    return buildVoPageResult(apisAuthPage, ServicesAuthAssembler::toDetailVo);
  }

}
