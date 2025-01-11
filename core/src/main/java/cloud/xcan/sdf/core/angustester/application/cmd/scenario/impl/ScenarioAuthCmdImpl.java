package cloud.xcan.sdf.core.angustester.application.cmd.scenario.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.ScenarioAuthConverter.toScenarioCreatorAuths;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.AUTH_CANCEL;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.AUTH_UPDATED;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.sdf.core.angustester.domain.scenario.auth.ScenarioAuthRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ScenarioAuthCmdImpl extends CommCmd<ScenarioAuth, Long> implements ScenarioAuthCmd {

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ScenarioAuthRepo scenarioAuthRepo;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScenarioAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(auth.getScenarioId());
        // Check the add creator permissions
        BizAssert.assertTrue(!scenarioDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the user have scenario authorization permissions
        scenarioAuthQuery.checkGrantAuth(getUserId(), auth.getScenarioId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        // Check the for duplicate authorizations
        scenarioAuthQuery.checkRepeatAuth(auth.getScenarioId(),
            auth.getAuthObjectId(), auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add grant permission activity
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(SCENARIO, scenarioDb,
              ActivityType.AUTH, authObjectName));
        }
        return insert(auth, "authObjectId");
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(ScenarioAuth auth) {
    new BizTemplate<Void>() {
      ScenarioAuth authDb;
      Scenario scenarioDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the ServicesAuth existed
        authDb = scenarioAuthQuery.checkAndFind(auth.getId());
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(authDb.getScenarioId());
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the current user have scenario authorization permissions
        scenarioAuthQuery.checkGrantAuth(getUserId(), authDb.getScenarioId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        //  Replace authorization
        authDb.setAuths(auth.getAuths());
        scenarioAuthRepo.save(authDb);

        // Add modify permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SCENARIO, scenarioDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long scenarioId) {
    new BizTemplate<Void>() {
      ScenarioAuth authDb;
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario auth exists
        authDb = scenarioAuthQuery.checkAndFind(scenarioId);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(authDb.getScenarioId());
        // Check the user have scenario authorization permissions
        scenarioAuthQuery.checkGrantAuth(getUserId(), authDb.getScenarioId());
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
        activityCmd.add(toActivity(SCENARIO, scenarioDb, AUTH_CANCEL, authObjectName));

        // Delete scenario permission
        scenarioAuthRepo.deleteById(scenarioId);

        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long id, Boolean enabledFlag) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check the scenario exists
        scenarioDb = scenarioQuery.checkAndFind0(id);
        // Check the user have scenario authorization permissions
        scenarioAuthQuery.checkGrantAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        scenarioRepo.updateAuthFlagById(id, enabledFlag);

        // Enable permission control activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb,
            enabledFlag ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  @Override
  public void addCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Allow modification of new authorization
    scenarioAuthRepo.deleteByScenarioIdAndCreatorFlag(scenarioId, true);

    // Save authorization
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  @Override
  public void moveCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Exists scenario move from scenario to parent dir
    scenarioAuthRepo.deleteByScenarioIdAndAuthObjectIdInAndCreatorFlag(
        scenarioId, creatorIds, true);

    // Save authorization
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  @Override
  protected BaseRepository<ScenarioAuth, Long> getRepository() {
    return this.scenarioAuthRepo;
  }
}




