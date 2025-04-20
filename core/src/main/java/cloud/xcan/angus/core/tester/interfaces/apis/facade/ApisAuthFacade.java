package cloud.xcan.angus.core.tester.interfaces.apis.facade;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth.ApisAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApiAuthVo;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.auth.ApisAuthCurrentVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.List;

public interface ApisAuthFacade {

  IdKey<Long, Object> add(Long apiId, ApisAuthAddDto dto);

  void delete(Long id);

  void replace(Long id, ApisAuthReplaceDto dto);

  void enabled(Long apiId, Boolean enabled);

  Boolean status(Long apiId);

  List<ApiPermission> userAuth(Long apiId, Long userId, Boolean admin);

  ApisAuthCurrentVo currentUserAuth(Long apiId, Boolean admin);

  void authCheck(Long apiId, ApiPermission permission, Long userId);

  PageResult<ApiAuthVo> list(ApisAuthFindDto dto);

}
