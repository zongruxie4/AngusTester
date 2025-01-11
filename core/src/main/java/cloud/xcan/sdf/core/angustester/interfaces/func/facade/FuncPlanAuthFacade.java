package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.auth.FuncPlanAuthVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface FuncPlanAuthFacade {

  IdKey<Long, Object> add(Long planId, FuncPlanAuthAddDto dto);

  void replace(Long id, FuncPlanAuthReplaceDto dto);

  void enabled(Long planId, Boolean enabledFlag);

  Boolean status(Long planId);

  void delete(Long id);

  List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean adminFlag);

  FuncPlanAuthCurrentVo currentUserAuth(Long planId, Boolean adminFlag);

  Map<Long, FuncPlanAuthCurrentVo> currentUserAuths(HashSet<Long> planIds, Boolean adminFlag);

  void authCheck(Long planId, FuncPlanPermission permission, Long userId);

  PageResult<FuncPlanAuthVo> list(FuncPlanAuthFindDto dto);

}
