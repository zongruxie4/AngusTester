package cloud.xcan.angus.core.tester.interfaces.services.facade;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface ServicesAuthFacade {

  IdKey<Long, Object> add(Long projectId, ServicesAddAuthDto dto);

  void replace(Long id, ServicesAuthReplaceDto dto);

  void delete(Long id);

  void enabled(Long projectId, Boolean enabled);

  Boolean status(Long projectId);

  void apisEnabled(Long projectId, Boolean enabled);

  List<ServicesPermission> userAuth(Long projectId, Long userId, Boolean admin);

  ServiceAuthCurrentVo currentUserAuth(Long projectId, Boolean admin);

  void authCheck(Long projectId, ServicesPermission authPermission, Long userId);

  PageResult<ServicesAuthVo> list(ServicesAuthFindDto dto);

}
