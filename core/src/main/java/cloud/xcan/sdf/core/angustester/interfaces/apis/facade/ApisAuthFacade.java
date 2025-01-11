package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;

public interface ApisAuthFacade {

  IdKey<Long, Object> add(Long apiId, ApisAuthAddDto dto);

  void delete(Long id);

  void replace(Long id, ApisAuthReplaceDto dto);

  void enabled(Long apiId, Boolean enabledFlag);

  Boolean status(Long apiId);

  List<ApiPermission> userAuth(Long apiId, Long userId, Boolean adminFlag);

  ApisAuthCurrentVo currentUserAuth(Long apiId, Boolean adminFlag);

  void authCheck(Long apiId, ApiPermission permission, Long userId);

  PageResult<ApiAuthVo> list(ApisAuthFindDto dto);

}
