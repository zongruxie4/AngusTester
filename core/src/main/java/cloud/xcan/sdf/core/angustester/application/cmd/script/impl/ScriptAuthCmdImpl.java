package cloud.xcan.sdf.core.angustester.application.cmd.script.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCRIPT;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ScriptAuthConverter.toScriptCreatorAuths;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptRepo;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuthRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScriptAuthCmdImpl extends CommCmd<ScriptAuth, Long> implements ScriptAuthCmd {

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ScriptRepo scriptRepo;

  @Resource
  private ScriptAuthQuery scriptAuthQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ScriptAuthRepo scriptAuthRepo;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScriptAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ScriptInfo scriptDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scriptDb = scriptQuery.checkAndFindInfo(auth.getScriptId());
        // Check the add creator permissions
        BizAssert.assertTrue(!scriptDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the user have scenario authorization permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), auth.getScriptId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        // Check the for duplicate authorizations
        scriptAuthQuery.checkRepeatAuth(auth.getScriptId(),
            auth.getAuthObjectId(), auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add grant permission activity
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH, authObjectName));
        }

        return insert(auth, "authObjectId");
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(ScriptAuth auth) {
    new BizTemplate<Void>() {
      ScriptAuth authDb;
      ScriptInfo scriptDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the ServicesAuth existed
        authDb = scriptAuthQuery.checkAndFind(auth.getId());
        // Check the scenario exists
        scriptDb = scriptQuery.checkAndFindInfo(authDb.getScriptId());
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the current user have scenario authorization permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), authDb.getScriptId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        //  Replace authorization
        authDb.setAuths(auth.getAuths());
        scriptAuthRepo.save(authDb);

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long scriptId) {
    new BizTemplate<Void>() {
      ScriptAuth authDb;
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Check the scenario auth exists
        authDb = scriptAuthQuery.checkAndFind(scriptId);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the scenario exists
        scriptDb = scriptQuery.checkAndFindInfo(authDb.getScriptId());
        // Check the user have scenario authorization permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), authDb.getScriptId());
      }

      @Override
      protected Void process() {
        // Get if authorization object name
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // NOOP: Authorization can also be cancelled after the authorization object is deleted
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH_CANCEL, authObjectName));

        // Delete scenario permission
        scriptAuthRepo.deleteById(scriptId);

        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long id, Boolean enabledFlag) {
    new BizTemplate<Void>() {
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scriptDb = scriptQuery.checkAndFindInfo(id);
        // Check the user have scenario authorization permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        scriptRepo.updateAuthFlagById(id, enabledFlag);

        // Enable permission control activity
        activityCmd.add(toActivity(SCRIPT, scriptDb,
            enabledFlag ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  @Override
  public void addCreatorAuth(Collection<Long> creatorIds, Long scriptId) {
    // Allow modification of new authorization
    scriptAuthRepo.deleteByScriptIdAndCreatorFlag(scriptId, true);

    // Save authorization
    batchInsert(toScriptCreatorAuths(creatorIds, scriptId, uidGenerator), "authObjectId");
  }

  @Override
  public void moveCreatorAuth(Collection<Long> creatorIds, Long scriptId) {
    // Exists scenario move from scenario to parent dir
    scriptAuthRepo.deleteByScriptIdAndAuthObjectIdInAndCreatorFlag(scriptId, creatorIds, true);

    // Save authorization
    batchInsert(toScriptCreatorAuths(creatorIds, scriptId, uidGenerator), "authObjectId");
  }

  @Override
  public void deleteByScriptIdIn(Collection<Long> scriptIds) {
    scriptAuthRepo.deleteByScriptIdIn(scriptIds);
  }

  @Override
  protected BaseRepository<ScriptAuth, Long> getRepository() {
    return this.scriptAuthRepo;
  }
}




