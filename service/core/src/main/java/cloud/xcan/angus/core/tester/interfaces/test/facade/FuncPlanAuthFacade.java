package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth.FuncPlanAuthReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthCurrentVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.auth.FuncPlanAuthVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface FuncPlanAuthFacade {

  IdKey<Long, Object> add(Long planId, FuncPlanAuthAddDto dto);

  void replace(Long id, FuncPlanAuthReplaceDto dto);

  void enabled(Long planId, Boolean enabled);

  Boolean status(Long planId);

  void delete(Long id);

  List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean admin);

  FuncPlanAuthCurrentVo currentUserAuth(Long planId, Boolean admin);

  Map<Long, FuncPlanAuthCurrentVo> currentUserAuths(HashSet<Long> planIds, Boolean admin);

  void authCheck(Long planId, FuncPlanPermission permission, Long userId);

  PageResult<FuncPlanAuthVo> list(FuncPlanAuthFindDto dto);

}
