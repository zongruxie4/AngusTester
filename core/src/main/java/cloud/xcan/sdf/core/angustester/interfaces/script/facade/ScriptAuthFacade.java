package cloud.xcan.sdf.core.angustester.interfaces.script.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface ScriptAuthFacade {

  IdKey<Long, Object> add(Long scriptId, ScriptAuthAddDto dto);

  void delete(Long scriptId);

  void replace(Long scriptId, ScriptAuthReplaceDto dto);

  void enabled(Long scriptId, Boolean enabledFlag);

  Boolean status(Long scriptId);

  List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean adminFlag);

  ScriptAuthCurrentVo currentUserAuth(Long scriptId, Boolean adminFlag);

  Map<Long, ScriptAuthCurrentVo> currentUserAuths(HashSet<Long> scriptIds, Boolean adminFlag);

  void authCheck(Long scriptId, ScriptPermission authPermission, Long userId);

  PageResult<ScriptAuthVo> list(ScriptAuthFindDto dto);

}
