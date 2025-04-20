package cloud.xcan.angus.core.tester.interfaces.mock.facade;

import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface MockServiceAuthFacade {

  IdKey<Long, Object> add(Long serviceId, ServiceAddAuthDto dto);

  void replace(Long id, ServiceAuthReplaceDto dto);

  void enabled(Long serviceId, Boolean enabled);

  Boolean status(Long serviceId);

  void delete(Long id);

  List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean admin);

  void authCheck(Long projectId, MockServicePermission authPermission, Long userId);

  PageResult<ServiceAuthVo> list(Long projectId, ServiceAuthFindDto dto);

}
