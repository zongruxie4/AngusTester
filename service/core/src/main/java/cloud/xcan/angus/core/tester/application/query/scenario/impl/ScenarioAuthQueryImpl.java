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

  @Override
  public Page<ScenarioAuth> find(Specification<ScenarioAuth> spec,
      List<String> scenarioIds, Pageable pageable) {
    return new BizTemplate<Page<ScenarioAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(scenarioIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"scenarioId"});
        batchCheckPermission(scenarioIds.stream().map(Long::parseLong)
            .collect(Collectors.toList()), ScenarioPermission.VIEW);
      }

      @Override
      protected Page<ScenarioAuth> process() {
        return scenarioAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ScenarioAuth checkAndFind(Long id) {
    return scenarioAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScriptAuth"));
  }

  @Override
  public void checkViewAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.DELETE);
  }

  @Override
  public void checkTestAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.TEST);
  }

  @Override
  public void checkGrantAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.GRANT, false, true);
  }

  @Override
  public void checkExportAuth(Long userId, Long scenarioId) {
    checkAuth(userId, scenarioId, ScenarioPermission.EXPORT);
  }

  @Override
  public void checkAuth(Long userId, Long scenarioId, ScenarioPermission permission) {
    checkAuth(userId, scenarioId, permission, false, permission.isGrant());
  }

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
   * Verify the operation permissions of the apis
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
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (isNotEmpty(scePermissions) && scePermissions.contains(permission)) {
          continue;
        }
      }
      Scenario scenarioInfo = scenarioRepo.find0ById(sceId).orElse(null);
      throw BizException.of(SCE_NO_TARGET_AUTH_CODE, SCE_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(scenarioInfo) ? sceId : scenarioInfo.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long scenarioId, Long authObjectId, AuthObjectType authObjectType) {
    if (scenarioAuthRepo.countByScenarioIdAndAuthObjectIdAndAuthObjectType(scenarioId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ScenarioPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scenarioAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(ScenarioAuth::getScenarioId).collect(
            Collectors.toList());
  }

  @Override
  public List<ScenarioAuth> findAuth(Long userId, Long scenarioId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scenarioAuthRepo.findAllByScenarioIdAndAuthObjectIdIn(scenarioId, orgIds);
  }

  @Override
  public List<ScenarioAuth> findAuth(Long userId, Collection<Long> scenarioIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(scenarioIds) ? scenarioAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : scenarioAuthRepo.findAllByScenarioIdInAndAuthObjectIdIn(scenarioIds, orgIds);
  }

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
        .distinct().collect(Collectors.toList());
  }

  @Override
  public boolean isCreator(Long userId, Long scenarioId) {
    List<ScenarioAuth> scenarioAuths = findAuth(userId, scenarioId);
    return isCreator(scenarioAuths);
  }

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

  private Set<ScenarioPermission> flatPermissions(List<ScenarioAuth> auths) {
    Set<ScenarioPermission> actions = new HashSet<>();
    for (ScenarioAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }

}
