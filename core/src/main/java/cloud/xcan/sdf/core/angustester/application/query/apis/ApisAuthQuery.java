package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuth;
import cloud.xcan.sdf.core.angustester.domain.apis.auth.ApisAuthCurrent;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ApisAuthQuery {

  Boolean status(Long apiId);

  List<ApiPermission> userAuth(Long apiId, Long userId, Boolean adminFlag);

  ApisAuthCurrent currentUserAuth(Long apiId, Boolean adminFlag);

  void check(Long apisId, ApiPermission permission, Long userId);

  Page<ApisAuth> find(List<String> apiIds, Specification<ApisAuth> spec,
      Pageable pageable);

  ApisAuth checkAndFind(Long id);

  void checkViewAuth(Long userId, Long apisId);

  void checkModifyAuth(Long userId, Long apisId);

  void checkDeleteAuth(Long userId, Long apisId);

  void checkDebugAuth(Long userId, Long apisId);

  /**
   * Whether the user has the permission to execute testing, the permission to add includes:
   *
   * <pre>
   *   1. Apis performance testing
   *   2. Apis automation testing
   * </pre>
   *
   * @param userId grant user id
   * @param apisId grant project id
   * @throws cloud.xcan.sdf.core.biz.exception.BizException If not grant {@link ApiPermission#TEST}
   */
  void checkTestAuth(Long userId, Long apisId);

  /**
   * Whether the user has the permission to grant other user.
   *
   * @param userId grant user id
   * @param apisId grant api id
   * @throws cloud.xcan.sdf.core.biz.exception.BizException If not grant {@link ApiPermission#GRANT}
   */
  void checkGrantAuth(Long userId, Long apisId);

  /**
   * Whether the user has the permission to shared.
   *
   * @param userId grant user id
   * @param apisId grant api id
   * @throws cloud.xcan.sdf.core.biz.exception.BizException If not grant {@link ApiPermission#SHARE}
   */
  void checkShareAuth(Long userId, Long apisId);

  void checkReleaseAuth(Long userId, Long apisId);

  void checkExportAuth(Long userId, Long apisId);

  void checkAuth(Long userId, Long apisId, ApiPermission permission);

  void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType,
      Boolean creatorFlag);

  void checkAuth(Long userId, Long apisId, ApiPermission permission, boolean ignoreAdmin,
      boolean ignorePublic);

  void batchCheckPermission(Collection<Long> apis, ApiPermission permission);

  void checkRepeatAuth(Long apisId, Long authObjectId, AuthObjectType authObjectType);

  List<ApisAuth> findAuth(Long userId, Long apisId);

  List<ApisAuth> findAuth(Long userId, Collection<Long> apisIds);

  List<ApiPermission> getUserAuth(Long apiId, Long userId);

  boolean isCreator(Long userId, Long apisId);

}

