package cloud.xcan.angus.core.tester.interfaces.script.facade;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth.ScriptAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth.ScriptAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface ScriptAuthFacade {

  IdKey<Long, Object> add(Long scriptId, ScriptAuthAddDto dto);

  void delete(Long scriptId);

  void replace(Long scriptId, ScriptAuthReplaceDto dto);

  void enabled(Long scriptId, Boolean enabled);

  Boolean status(Long scriptId);

  List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean admin);

  ScriptAuthCurrentVo currentUserAuth(Long scriptId, Boolean admin);

  Map<Long, ScriptAuthCurrentVo> currentUserAuths(HashSet<Long> scriptIds, Boolean admin);

  void authCheck(Long scriptId, ScriptPermission authPermission, Long userId);

  PageResult<ScriptAuthVo> list(ScriptAuthFindDto dto);

}
