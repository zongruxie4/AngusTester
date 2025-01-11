package cloud.xcan.sdf.core.angustester.interfaces.services.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAddAuthDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.auth.ServicesAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServiceAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.auth.ServicesAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ServicesAuthFacade {

  IdKey<Long, Object> add(Long projectId, ServicesAddAuthDto dto);

  void replace(Long id, ServicesAuthReplaceDto dto);

  void delete(Long id);

  void enabled(Long projectId, Boolean enabled);

  Boolean status(Long projectId);

  void apisEnabled(Long projectId, Boolean enabled);

  List<ServicesPermission> userAuth(Long projectId, Long userId, Boolean adminFlag);

  ServiceAuthCurrentVo currentUserAuth(Long projectId, Boolean adminFlag);

  void authCheck(Long projectId, ServicesPermission authPermission, Long userId);

  PageResult<ServicesAuthVo> list(ServicesAuthFindDto dto);

}
