package cloud.xcan.angus.core.tester.application.query.script.impl;

import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_NO_AUTH_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_NO_AUTH_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_NO_TARGET_AUTH;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SCRIPT_NO_TARGET_AUTH_CODE;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isUserAction;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_KEY;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_MISSING_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.api.commonlink.user.UserRepo;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptAuthQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuth;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuthCurrent;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuthRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
public class ScriptAuthQueryImpl implements ScriptAuthQuery {

  @Resource
  private ScriptAuthRepo scriptAuthRepo;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ScriptRepo scriptRepo;

  @Resource
  private UserRepo userRepo;

  @Resource
  private CommonQuery commonQuery;

  @Override
  public Boolean status(Long scriptId) {
    return new BizTemplate<Boolean>() {
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Check the script exists
        scriptDb = scriptQuery.checkAndFindInfo(scriptId);
      }

      @Override
      protected Boolean process() {
        return scriptDb.getAuth();
      }
    }.execute();
  }

  @Override
  public List<ScriptPermission> userAuth(Long scriptId, Long userId, Boolean admin) {
    return new BizTemplate<List<ScriptPermission>>() {
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Check the script exists
        scriptDb = scriptQuery.checkAndFindInfo(scriptId);
      }

      @Override
      protected List<ScriptPermission> process() {
        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          return ScriptPermission.ALL;
        }

        List<ScriptAuth> scriptAuths = findAuth(userId, scriptId);
        if (isCreator(scriptAuths)) {
          return ScriptPermission.ALL;
        }

        Set<ScriptPermission> permissions = new HashSet<>();
        if (!scriptDb.isEnabledAuth()) {
          permissions.add(ScriptPermission.VIEW);
        }

        Set<ScriptPermission> authPermissions = scriptAuths.stream().map(ScriptAuth::getAuths)
            .flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        return new ArrayList<>(authPermissions);
      }
    }.execute();
  }

  @Override
  public ScriptAuthCurrent currentUserAuth(Long scriptId, Boolean admin) {
    return new BizTemplate<ScriptAuthCurrent>() {
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Check the script exists
        scriptDb = scriptQuery.checkAndFindInfo(scriptId);
      }

      @Override
      protected ScriptAuthCurrent process() {
        ScriptAuthCurrent authCurrent = new ScriptAuthCurrent();
        authCurrent.setScriptAuth(scriptDb.getAuth());

        if (Objects.nonNull(admin) && admin && commonQuery.isAdminUser()) {
          authCurrent.addPermissions(ScriptPermission.ALL);
          return authCurrent;
        }

        List<ScriptAuth> scriptAuths = findAuth(getUserId(), scriptId);
        if (isCreator(scriptAuths)) {
          authCurrent.addPermissions(ScriptPermission.ALL);
          return authCurrent;
        }

        Set<ScriptPermission> permissions = new HashSet<>();
        if (!scriptDb.isEnabledAuth()) {
          permissions.add(ScriptPermission.VIEW);
        }
        Set<ScriptPermission> authPermissions = scriptAuths.stream()
            .map(ScriptAuth::getAuths).flatMap(Collection::stream).collect(Collectors.toSet());
        authPermissions.addAll(permissions);
        authCurrent.addPermissions(authPermissions);
        return authCurrent;
      }
    }.execute();
  }

  @Override
  public Map<Long, ScriptAuthCurrent> currentUserAuths(HashSet<Long> scriptIds, Boolean admin) {
    return new BizTemplate<Map<Long, ScriptAuthCurrent>>() {
      List<ScriptInfo> scriptsDb;

      @Override
      protected void checkParams() {
        // Check the script exists
        scriptsDb = scriptQuery.checkAndFindInfos(scriptIds);
      }

      @Override
      protected Map<Long, ScriptAuthCurrent> process() {
        Map<Long, ScriptAuthCurrent> authCurrentMap = new HashMap<>();
        if (nonNull(admin) && admin && commonQuery.isAdminUser()) {
          for (ScriptInfo script : scriptsDb) {
            ScriptAuthCurrent authCurrent = new ScriptAuthCurrent();
            authCurrent.setScriptAuth(script.getAuth());
            authCurrent.addPermissions(ScriptPermission.ALL);
            authCurrentMap.put(script.getId(), authCurrent);
          }
          return authCurrentMap;
        }

        Set<Long> currentCreatorIds = scriptsDb.stream()
            .filter(x -> x.getCreatedBy().equals(getUserId())).map(ScriptInfo::getId)
            .collect(Collectors.toSet());
        if (isNotEmpty(currentCreatorIds)) {
          for (ScriptInfo script : scriptsDb) {
            if (currentCreatorIds.contains(script.getId())) {
              ScriptAuthCurrent authCurrent = new ScriptAuthCurrent();
              authCurrent.setScriptAuth(script.getAuth());
              authCurrent.addPermissions(ScriptPermission.ALL);
              authCurrentMap.put(script.getId(), authCurrent);
            }
          }
        }

        Set<Long> remainIds = new HashSet<>(scriptIds);
        remainIds.removeAll(currentCreatorIds);
        if (isNotEmpty(remainIds)) {
          Map<Long, List<ScriptAuth>> scriptAuthsMap = findAuth(getUserId(), remainIds)
              .stream().collect(Collectors.groupingBy(ScriptAuth::getScriptId));
          for (ScriptInfo script : scriptsDb) {
            if (remainIds.contains(script.getId())) {
              ScriptAuthCurrent authCurrent = new ScriptAuthCurrent();
              Set<ScriptPermission> permissions = new HashSet<>();
              if (!script.isEnabledAuth()) {
                permissions.add(ScriptPermission.VIEW);
              }
              List<ScriptAuth> scriptAuths = scriptAuthsMap.get(script.getId());
              if (isNotEmpty(scriptAuths)) {
                Set<ScriptPermission> authPermissions = scriptAuths.stream()
                    .map(ScriptAuth::getAuths).flatMap(Collection::stream)
                    .collect(Collectors.toSet());
                permissions.addAll(authPermissions);
              }
              authCurrent.addPermissions(permissions);
              authCurrent.setScriptAuth(script.getAuth());
              authCurrentMap.put(script.getId(), authCurrent);
            }
          }
        }
        return authCurrentMap;
      }
    }.execute();
  }

  @Override
  public void check(Long scriptId, ScriptPermission permission, Long userId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        checkAuth(userId, scriptId, permission);
        return null;
      }
    }.execute();
  }

  @Override
  public Page<ScriptAuth> find(Specification<ScriptAuth> spec,
      List<String> scriptIds, Pageable pageable) {
    return new BizTemplate<Page<ScriptAuth>>() {
      @Override
      protected void checkParams() {
        ProtocolAssert.assertTrue(isNotEmpty(scriptIds), PARAM_MISSING_T, PARAM_MISSING_KEY,
            new Object[]{"scriptId"});
        batchCheckPermission(scriptIds.stream().map(Long::parseLong)
            .collect(Collectors.toList()), ScriptPermission.VIEW);
      }

      @Override
      protected Page<ScriptAuth> process() {
        return scriptAuthRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ScriptAuth checkAndFind(Long id) {
    return scriptAuthRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ScriptAuth"));
  }

  @Override
  public void checkViewAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.VIEW);
  }

  @Override
  public void checkModifyAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.MODIFY);
  }

  @Override
  public void checkDeleteAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.DELETE);
  }

  @Override
  public void checkTestAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.TEST);
  }

  @Override
  public void checkGrantAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.GRANT, false, true);
  }

  @Override
  public void checkExportAuth(Long userId, Long scriptId) {
    checkAuth(userId, scriptId, ScriptPermission.EXPORT);
  }

  @Override
  public void checkAuth(Long userId, Long scriptId, ScriptPermission permission) {
    checkAuth(userId, scriptId, permission, false, permission.isGrant());
  }

  @Override
  public void checkAuth(Long userId, Long scriptId, ScriptPermission permission,
      boolean ignoreAdminPermission, boolean ignorePublicAccess) {
    if (!ignoreAdminPermission && commonQuery.isAdminUser() || !isUserAction()) {
      return;
    }

    // Fix: When it is not controlled by permissions, it will cause users who do not have authorization permissions to authorize
    if (!ignorePublicAccess && !permission.isGrant() && !scriptQuery.isAuthCtrl(scriptId)) {
      return;
    }

    List<ScriptAuth> auths = findAuth(userId, scriptId);
    if (permission.equals(ScriptPermission.VIEW)) {
      // View as base permissions
      if (isEmpty(auths)) {
        throw BizException.of(SCRIPT_NO_AUTH_CODE, SCRIPT_NO_AUTH_T, new Object[]{permission});
      }
    }

    if (isCreator(auths)) {
      return;
    }
    if (!flatPermissions(auths).contains(permission)) {
      throw BizException.of(SCRIPT_NO_AUTH_CODE, SCRIPT_NO_AUTH_T, new Object[]{permission});
    }
  }

  /**
   * Verify the operation permissions of the apis
   */
  @Override
  public void batchCheckPermission(Collection<Long> scriptIds, ScriptPermission permission) {
    if (commonQuery.isAdminUser() || isEmpty(scriptIds) || Objects.isNull(permission)
        || !isUserAction()) {
      return;
    }

    Collection<Long> authIds = permission.isGrant()
        ? scriptIds : scriptRepo.findIds0ByIdInAndAuth(scriptIds, true);
    if (isEmpty(authIds)) {
      return;
    }

    List<ScriptAuth> auths = findAuth(getUserId(), authIds);
    if (isEmpty(auths)) {
      long firstId = authIds.stream().findFirst().get();
      Script script = scriptRepo.find0ById(firstId).orElse(null);
      throw BizException.of(SCRIPT_NO_TARGET_AUTH_CODE, SCRIPT_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(script) ? firstId : script.getName()});
    }

    Map<Long, List<ScriptAuth>> authMap = auths.stream().filter(o -> nonNull(o.getScriptId()))
        .collect(Collectors.groupingBy(ScriptAuth::getScriptId));
    for (Long id : authMap.keySet()) {
      List<ScriptAuth> values = authMap.get(id);
      if (ObjectUtils.isNotEmpty(values)) {
        List<ScriptPermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toList());
        if (ObjectUtils.isNotEmpty(permissions) && permissions.contains(permission)) {
          continue;
        }
      }
      Script scriptInfo = scriptRepo.find0ById(id).orElse(null);
      throw BizException.of(SCRIPT_NO_TARGET_AUTH_CODE, SCRIPT_NO_TARGET_AUTH,
          new Object[]{permission, Objects.isNull(scriptInfo) ? id : scriptInfo.getName()});
    }
  }

  @Override
  public void checkRepeatAuth(Long scriptId, Long authObjectId, AuthObjectType authObjectType) {
    if (scriptAuthRepo.countByScriptIdAndAuthObjectIdAndAuthObjectType(scriptId, authObjectId,
        authObjectType) > 0) {
      throw ResourceExisted.of(authObjectId, "Authorization:" + authObjectType.name());
    }
  }

  @Override
  public List<Long> findByAuthObjectIdsAndPermission(Long userId, ScriptPermission permission) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scriptAuthRepo.findAllByAuthObjectIdIn(orgIds).stream()
        .filter(a -> a.getAuths().contains(permission)).map(ScriptAuth::getScriptId).collect(
            Collectors.toList());
  }

  @Override
  public List<ScriptAuth> findAuth(Long userId, Long scriptId) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return scriptAuthRepo.findAllByScriptIdAndAuthObjectIdIn(scriptId, orgIds);
  }

  @Override
  public List<ScriptAuth> findAuth(Long userId, Collection<Long> scriptIds) {
    List<Long> orgIds = userRepo.findOrgIdsById(userId);
    orgIds.add(userId);
    return isEmpty(scriptIds) ? scriptAuthRepo.findAllByAuthObjectIdIn(orgIds)
        : scriptAuthRepo.findAllByScriptIdInAndAuthObjectIdIn(scriptIds, orgIds);
  }

  @Override
  public List<ScriptPermission> getUserAuth(Long scriptId, Long userId) {
    if (commonQuery.isAdminUser()) {
      return ScriptPermission.ALL;
    }

    List<ScriptAuth> auths = findAuth(userId, scriptId);
    if (isEmpty(auths)) {
      return null;
    }
    if (isCreator(auths)) {
      return ScriptPermission.ALL;
    }
    return auths.stream().map(ScriptAuth::getAuths).flatMap(Collection::stream)
        .distinct().collect(Collectors.toList());
  }

  @Override
  public Map<Long, Set<ScriptPermission>> getUserScriptAuth(Collection<Long> scriptIds,
      Long userId) {
    if (commonQuery.isAdminUser()) {
      return scriptIds.stream()
          .collect(Collectors.toMap(x -> x, x -> new HashSet<>(ScriptPermission.ALL)));
    }

    List<ScriptAuth> auths = findAuth(userId, scriptIds);
    if (isEmpty(auths)) {
      return Collections.emptyMap();
    }

    Map<Long, List<ScriptAuth>> authMap = auths.stream()
        .filter(o -> nonNull(o.getScriptId()))
        .collect(Collectors.groupingBy(ScriptAuth::getScriptId));
    Map<Long, Set<ScriptPermission>> authPermissionMap = new HashMap<>();
    for (Long id : authMap.keySet()) {
      List<ScriptAuth> values = authMap.get(id);
      if (ObjectUtils.isNotEmpty(values)) {
        Set<ScriptPermission> permissions = values.stream()
            .flatMap(o -> o.getAuths().stream()).collect(Collectors.toSet());
        authPermissionMap.put(id, permissions);
      }
    }
    return authPermissionMap;
  }

  @Override
  public boolean isCreator(Long userId, Long scriptId) {
    List<ScriptAuth> scriptAuths = findAuth(userId, scriptId);
    return isCreator(scriptAuths);
  }

  private boolean isCreator(List<ScriptAuth> auths) {
    if (auths.isEmpty()) {
      return false;
    }
    for (ScriptAuth auth : auths) {
      if (auth.getCreator()) {
        return true;
      }
    }
    return false;
  }

  private Set<ScriptPermission> flatPermissions(List<ScriptAuth> auths) {
    Set<ScriptPermission> actions = new HashSet<>();
    for (ScriptAuth auth : auths) {
      actions.addAll(auth.getAuths());
    }
    return actions;
  }

}
