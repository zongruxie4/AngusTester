package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal;

import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockServiceAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.MockServiceAuthFacade;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.internal.assembler.MockServiceAuthAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * @author xiaolong.liu
 */
@Component
public class MockServiceAuthFacadeImpl implements MockServiceAuthFacade {

  @Resource
  private MockServiceAuthCmd mockServiceAuthCmd;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long serviceId, ServiceAddAuthDto dto) {
    return mockServiceAuthCmd.add(MockServiceAuthAssembler.addDtoToDomain(serviceId, dto));
  }

  @Override
  public void replace(Long id, ServiceAuthReplaceDto dto) {
    mockServiceAuthCmd.replace(MockServiceAuthAssembler.replaceDtoToDomain(id, dto));
  }

  @Override
  public void delete(Long id) {
    mockServiceAuthCmd.delete(id);
  }

  @Override
  public List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean adminFlag) {
    return mockServiceAuthQuery.userAuth(serviceId, userId, adminFlag);
  }

  @Override
  public void enabled(Long serviceId, Boolean enabledFlag) {
    mockServiceAuthCmd.enabled(serviceId, enabledFlag);
  }

  @Override
  public Boolean status(Long serviceId) {
    return mockServiceAuthQuery.status(serviceId);
  }

  @Override
  public void authCheck(Long serviceId, MockServicePermission authPermission, Long userId) {
    mockServiceAuthQuery.check(serviceId, authPermission, userId);
  }

  @Override
  @NameJoin
  public PageResult<ServiceAuthVo> list(Long serviceId, ServiceAuthFindDto dto) {
    Page<MockServiceAuth> apisAuthPage = mockServiceAuthQuery
        .find(serviceId, MockServiceAuthAssembler.getSpecification(serviceId, dto), dto.tranPage());
    return buildVoPageResult(apisAuthPage, MockServiceAuthAssembler::toDetailVo);
  }

}
