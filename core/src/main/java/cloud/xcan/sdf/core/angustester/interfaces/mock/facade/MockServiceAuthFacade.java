package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServicePermission;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.auth.ServiceAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.auth.ServiceAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface MockServiceAuthFacade {

  IdKey<Long, Object> add(Long serviceId, ServiceAddAuthDto dto);

  void replace(Long id, ServiceAuthReplaceDto dto);

  void enabled(Long serviceId, Boolean enabledFlag);

  Boolean status(Long serviceId);

  void delete(Long id);

  List<MockServicePermission> userAuth(Long serviceId, Long userId, Boolean adminFlag);

  void authCheck(Long projectId, MockServicePermission authPermission, Long userId);

  PageResult<ServiceAuthVo> list(Long projectId, ServiceAuthFindDto dto);

}
