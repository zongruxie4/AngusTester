package cloud.xcan.angus.core.tester.application.query.test;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuth;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanAuthCurrent;
import cloud.xcan.angus.core.tester.domain.test.plan.auth.FuncPlanPermission;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface FuncPlanAuthQuery {

  Boolean status(Long planId);

  List<FuncPlanPermission> userAuth(Long planId, Long userId, Boolean admin);

  FuncPlanAuthCurrent currentUserAuth(Long planId, Boolean admin);

  Map<Long, FuncPlanAuthCurrent> currentUserAuths(HashSet<Long> planIds, Boolean admin);

  void check(Long planId, FuncPlanPermission permission, Long userId);

  Page<FuncPlanAuth> find(Specification<FuncPlanAuth> spec, List<String> planId,
      Pageable pageable);

  FuncPlanAuth checkAndFind(Long id);

  void checkGrantAuth(Long userId, Long planId);

  void checkModifyPlanAuth(Long userId, Long planId);

  void checkDeletePlanAuth(Long userId, Long planId);

  void checkAddCaseAuth(Long userId, Long planId);

  void checkModifyCaseAuth(Long userId, Long planId);

  void checkExportCaseAuth(Long userId, Long planId);

  void checkReviewAuth(Long userId, Long planId);

  void checkResetReviewResultAuth(Long userId, Long planId);

  void checkTestAuth(Long userId, Long planId);

  void checkResetTestResultAuth(Long userId, Long planId);

  void checkEstablishBaselineAuth(Long userId, Long planId);

  void checkAuth(Long userId, Long planId, FuncPlanPermission permission);

  void checkAuth(Long userId, Long planId, FuncPlanPermission permission,
      boolean ignoreAdmin, boolean ignorePublic);

  void batchCheckPermission(Collection<Long> planIds, FuncPlanPermission permission);

  void checkRepeatAuth(Long planId, Long authObjectId, AuthObjectType authObjectType);

  List<Long> findByAuthObjectIdsAndPermission(Long userId, FuncPlanPermission permission);

  List<FuncPlanAuth> findAuth(Long userId, Long planId);

  List<FuncPlanAuth> findAuth(Long userId, Collection<Long> planIds);

  List<FuncPlanPermission> getUserAuth(Long planId, Long userId);

  boolean isAdminUser();

  boolean isCreator(Long userId, Long planId);


}




