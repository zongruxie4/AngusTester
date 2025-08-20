package cloud.xcan.angus.core.tester.application.query.scenario.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_NO_AUTH_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCE_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthCurrent;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthRepo;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * <p>
 * Implementation of ScenarioAuthQuery for scenario authorization management and validation.
 * </p>
 * <p>
 * Provides methods for checking scenario permissions, managing user authorizations, and validating access rights.
 * </p>
 */
@Biz
public class ScenarioAuthQueryImpl implements ScenarioAuthQuery {

  @Resource
  private ScenarioAuthRepo scenarioAuthRepo;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private UserRepo userRepo;
  @Resource
  private CommonQuery commonQuery;

  /**
   * <p>
   * Get the authorization status of a scenario.
   * </p>
   * @param scenarioId Scenario ID
   * @return Authorization status
   */
  @Override
  public Boolean status(Long scenarioId) {
    return new BizTemplate<Boolean>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);
      }

      @Override
      protected Boolean process() {
        return scenarioDb.getAuth();
      }
    }.execute();
  }

  /**
   * <p>
   * Get user permissions for a specific scenario.
   * </p>
   * <p>
   * Returns all permissions for admins and creators. For regular users, returns permissions based on their authorization records.
   * </p>
   * @param scenarioId Scenario ID
   * @param userId User ID
   * @param admin Whether to check admin permissions
   * @return List of scenario permissions
   */
  @Override
  public List<ScenarioPermission> userAuth(Long scenarioId, Long userId, Boolean admin) {
    return new BizTemplate<List<ScenarioPermission>>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);
      }

      @Override
      protected List<ScenarioPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return ScenarioPermission.ALL;
        }

        List<ScenarioAuth> scenarioAuths = findAuth(userId, scenarioId);
        if (isCreator(scenarioAuths)) {
          return ScenarioPermission.ALL;
        }

        Set<ScenarioPermission> permissions = new HashSet<>();
        if (!scenarioDb.isEnabledAuth()) {
          permissions.add(ScenarioPermission.VIEW);
        }

        Set<ScenarioPermission> authPermissions = scenarioAuths.stream()
            .map(ScenarioAuth::getAuths).flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  /**
   * <p>
   * Get current user's authorization information for a scenario.
   * </p>
   * <p>
   * Returns authorization status and permissions for the current user.
   * </p>
   * @param scenarioId Scenario ID
   * @param admin Whether to check admin permissions
   * @return Current user's authorization information
   */
  @Override
  public ScenarioAuthCurrent currentUserAuth(Long scenarioId, Boolean admin) {
    return new BizTemplate<ScenarioAuthCurrent>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);
      }

      @Override
      protected ScenarioAuthCurrent process() {
        ScenarioAuthCurrent authCurrent = new ScenarioAuthCurrent();
        authCurrent.setScenarioAuth(scenarioDb.getAuth());

        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(ScenarioPermission.ALL);
          return authCurrent;
        }

        List<ScenarioAuth> scenarioAuths = findAuth(getUserId(), scenarioId);
        if (isCreator(scenarioAuths)) {
          authCurrent.addPermissions(ScenarioPermission.ALL);
          return authCurrent;
        }

        Set<ScenarioPermission> permissions = new HashSet<>();
        if (!scenarioDb.isEnabledAuth()) {
          permissions.add(ScenarioPermission.VIEW);
        }

        Set<ScenarioPermission> authPermissions = scenarioAuths.stream()
            .map(ScenarioAuth::getAuths).flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  /**
   * <p>
   * Check if a user has a specific permission for a scenario.
   * </p>
   * @param scenarioId Scenario ID
   * @param permission Required permission
   * @param userId User ID
   */
  @Override
  public void check(Long scenarioId, ScenarioPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, scenarioId, permission);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Find scenario authorizations with pagination and permission validation.
   * </p>
   * @param spec Search specification
   * @param scenarioIds List of scenario IDs
   * @param pageable Pagination information
   * @return Page of scenario authorizations
   */
  @Override
  public Page<ScenarioAuth> find(Specification<ScenarioAuth> spec,
      List<String> scenarioIds, Pageable pageable) {
    return new BizTemplate<Page<ScenarioAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(scenarioIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"scenarioId"});
        batchCheckPermission(scenarioIds.stream().map(Long::parseLong)
            .toList(), ScenarioPermission.VIEW);
      }

      @Override
      protected Page<ScenarioAuth> process() {
        return scenarioAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  /**
   * <p>
   * Check and find a scenario authorization by ID.
   * </p>
   * @param id Authorization ID
   * @return Scenario authorization entity
   */
  @Override
  public ScenarioAuth checkAndFind(Long id) {
    return scenarioAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScriptAuth"));
  }

  /**
   * <p>
   * Check if a user has view permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkViewAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.VIEW);
  }

  /**
   * <p>
   * Check if a user has modify permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkModifyAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.MODIFY);
  }

  /**
   * <p>
   * Check if a user has delete permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkDeleteAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.DELETE);
  }

  /**
   * <p>
   * Check if a user has test permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkTestAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.TEST);
  }

  /**
   * <p>
   * Check if a user has grant permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkGrantAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.GRANT, false, true);
  }

  /**
   * <p>
   * Check if a user has export permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   */
  @Override
  public void checkExportAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.EXPORT);
  }

  /**
   * <p>
   * Check if a user has a specific permission for a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   * @param permission Required permission
   */
  @Override
  public void checkAuth(Long userId, Long scenarioId, ScenarioPermission permission) {
    checkAuth(userId, scenarioId, permission, false, permission.isGrant());
  }

  /**
   * <p>
   * Check if a user has a specific permission for a scenario with additional control options.
   * </p>
   * <p>
   * Admins bypass permission checks unless explicitly ignored. Public access is allowed for non-grant permissions
   * when authorization is not controlled.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   * @param permission Required permission
   * @param ignoreAdminPermission Whether to ignore admin permissions
   * @param ignorePublicAccess Whether to ignore public access
   */
  @Override
  public void checkAuth(Long userId, Long scenarioId, ScenarioPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && commonQuery.isAdminUser()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !scenarioQuery.isAuthCtrl(scenarioId)) {
      return;
    }

    List<ScenarioAuth> auths = findAuth(userId, scenarioId);
    if (permission.equals(ScenarioPermission.VIEW)) {
      // View as base permissions
      if (isEmpty(auths)) {
        throw BizException.of(SCE_NO_AUTH_CODE, SCE_NO_AUTH_T, new Object[]{permission});
      }
    }

    if (isCreator(auths)) {
      return;
    }
    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(SCE_NO_AUTH_CODE, SCE_NO_AUTH_T, new Object[]{permission});
    }
  }

  /**
   * <p>
   * Verify the operation permissions of the apis for multiple scenarios.
   * </p>
   * <p>
   * Performs batch permission checking for a collection of scenarios. Admins bypass this check.
   * Only scenarios with authorization enabled are checked for non-grant permissions.
   * </p>
   * @param scenarioIds Collection of scenario IDs
   * @param permission Required permission
   */
  @Override
  public void batchCheckPermission(Collection<Long> scenarioIds, ScenarioPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(scenarioIds)
        || Objects.isNull(permission)) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? scenarioIds : scenarioRepo.findIds0ByIdInAndAuth(scenarioIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ScenarioAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      Scenario scenario = scenarioRepo.find0ById(firstId).orElse(null);
      throw BizException.of(SCE_NO_TARGET_AUTH_CODE, SCE_NO_TARGET_AUTH, new Object[]{permission,
          Objects.isNull(scenario) ? firstId : scenario.getName()});
    }

    Map<Long, List<ScenarioAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getScenarioId()))
        .collect(Collectors.groupingBy(ScenarioAuth::getScenarioId));
    for (Long sceId : authMap.keySet()) {
      List<ScenarioAuth> values = authMap.get(sceId);
      if (isNotEmpty(values)) {
        List<ScenarioPermission> scePermissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).toList();
        if (isNotEmpty(scePermissions) && scePermissions.contains(permission)) {
          continue;
        }
      }
      Scenario scenarioInfo = scenarioRepo.find0ById(sceId).orElse(null);
      throw BizException.of(SCE_NO_TARGET_AUTH_CODE, SCE_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(scenarioInfo) ? sceId : scenarioInfo.getName()});
    }
  }

  /**
   * <p>
   * Check if an authorization already exists for the specified scenario and auth object.
   * </p>
   * @param scenarioId Scenario ID
   * @param authObjectId Authorization object ID
   * @param authObjectType Authorization object type
   */
  @Override
  public void checkRepeatAuth(Long scenarioId, Long authObjectId, AuthObjectType authObjectType) {
    if (scenarioAuthRepo.countByScenarioIdAndAuthObjectIdAndAuthObjectType(scenarioId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  /**
   * <p>
   * Find scenario IDs where a user has a specific permission through their organization memberships.
   * </p>
   * @param userId User ID
   * @param permission Required permission
   * @return List of scenario IDs
   */
  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ScenarioPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scenarioAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(ScenarioAuth::getScenarioId).collect(
            Collectors.toList());
  }

  /**
   * <p>
   * Find authorization records for a user and a specific scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   * @return List of scenario authorizations
   */
  @Override
  public List<ScenarioAuth> findAuth(Long userId, Long scenarioId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scenarioAuthRepo.findAllByScenarioIdAndAuthObjectIdIn(scenarioId, orgIds);
  }

  /**
   * <p>
   * Find authorization records for a user and multiple scenarios.
   * </p>
   * @param userId User ID
   * @param scenarioIds Collection of scenario IDs
   * @return List of scenario authorizations
   */
  @Override
  public List<ScenarioAuth> findAuth(Long userId, Collection<Long> scenarioIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(scenarioIds) ? scenarioAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : scenarioAuthRepo.findAllByScenarioIdInAndAuthObjectIdIn(scenarioIds, orgIds);
  }

  /**
   * <p>
   * Get user permissions for a specific scenario.
   * </p>
   * <p>
   * Returns all permissions for admins and creators. For regular users, returns their specific permissions.
   * </p>
   * @param scenarioId Scenario ID
   * @param userId User ID
   * @return List of user permissions, or null if no permissions found
   */
  @Override
  public List<ScenarioPermission> getUserAuth(Long scenarioId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return ScenarioPermission.ALL;
    }

    List<ScenarioAuth> auths = findAuth(userId, scenarioId);
    if (isEmpty(auths)) {
      return null;
    }
    if (isCreator(auths)) {
      return ScenarioPermission.ALL;
    }
    return auths.stream().map(ScenarioAuth::getAuths).flatMap(Collection::stream)
        .distinct().toList();
  }

  /**
   * <p>
   * Check if a user is the creator of a scenario.
   * </p>
   * @param userId User ID
   * @param scenarioId Scenario ID
   * @return true if the user is the creator, false otherwise
   */
  @Override
  public boolean isCreator(Long userId, Long scenarioId) {
    List<ScenarioAuth> scenarioAuths = findAuth(userId, scenarioId);
    return isCreator(scenarioAuths);
  }

  /**
   * <p>
   * Check if any of the authorization records indicates the user is a creator.
   * </p>
   * @param auths List of scenario authorizations
   * @return true if the user is a creator, false otherwise
   */
  private boolean isCreator(List<ScenarioAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ScenarioAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>
   * Flatten all permissions from a list of authorization records into a single set.
   * </p>
   * @param auths List of scenario authorizations
   * @return Set of all permissions
   */
  private Set<ScenarioPermission> flatPermissions(List<ScenarioAuth> auths) {
    Set<ScenarioPermission> actions = new HashSet<>();
    for (ScenarioAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }

}
