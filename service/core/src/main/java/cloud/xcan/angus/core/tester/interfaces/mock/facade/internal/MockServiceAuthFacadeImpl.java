package cloud.xcan.angus.core.tester.interfaces.mock.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAuthAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAuthAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAuthAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceAuthCmd;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.MockServiceAuthFacade;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.internal.assembler.MockServiceAuthAssembler;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
 */
@Component
public class MockServiceAuthFacadeImpl implements MockServiceAuthFacade {

  @Resource
  private MockServiceAuthCmd mockServiceAuthCmd;

  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;

  @Override
  public IdKey<Long, Object> add(Long serviceId, ServiceAddAuthDto dto) {
    return mockServiceAuthCmd.add(addDtoToDomain(serviceId, dto));
  }

  @Override
  public void replace(Long id, ServiceAuthReplaceDto dto) {
    mockServiceAuthCmd.replace(replaceDtoToDomain(id, dto));
  }

  @Override
  public void delete(Long id) {
    mockServiceAuthCmd.delete(id);
  }

  @Override
  public List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean admin) {
    return mockServiceAuthQuery.userAuth(serviceId, userId, admin);
  }

  @Override
  public void enabled(Long serviceId, Boolean enabled) {
    mockServiceAuthCmd.enabled(serviceId, enabled);
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
    Page<MockServiceAuth> page = mockServiceAuthQuery
        .find(serviceId, getSpecification(serviceId, dto), dto.tranPage());
    return buildVoPageResult(page, MockServiceAuthAssembler::toDetailVo);
  }

}
