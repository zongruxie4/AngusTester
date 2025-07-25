package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ScenarioAuthConverter.toScenarioCreatorAuths;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_UPDATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuth;
import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for scenario authorization management.
 * <p>
 * Provides methods for adding, replacing, enabling/disabling, and deleting scenario authorizations.
 * <p>
 * Ensures permission checks, duplicate prevention, and activity logging.
 */
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

  /**
   * Add a new authorization for a scenario.
   * <p>
   * Checks scenario existence, permission, and duplicate authorization before adding.
   * <p>
   * Logs grant permission activity.
   */
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

  /**
   * Replace (update) an existing authorization for a scenario.
   * <p>
   * Checks existence, permission, and updates authorization details.
   * <p>
   * Logs modification activity.
   */
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
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
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

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SCENARIO, scenarioDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Delete an authorization for a scenario.
   * <p>
   * Checks existence and permission before deleting authorization.
   * <p>
   * Logs cancel activity and deletes the authorization.
   */
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
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
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

  /**
   * Enable or disable authorization control for a scenario.
   * <p>
   * Checks existence and permission before updating authorization status.
   * <p>
   * Logs enable/disable activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long id, Boolean enabled) {
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
        scenarioRepo.updateAuthById(id, enabled);

        // Enable permission control activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Add creator authorization for a scenario.
   * <p>
   * Batch inserts creator authorizations for the specified scenario.
   */
  @Override
  public void addCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Allow modification of new authorization
    scenarioAuthRepo.deleteByScenarioIdAndCreator(scenarioId, true);

    // Save authorization
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  /**
   * Move creator authorization for a scenario.
   * <p>
   * Removes and re-inserts creator authorizations for the specified scenario.
   */
  @Override
  public void moveCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Exists scenario move from scenario to parent dir
    scenarioAuthRepo.deleteByScenarioIdAndAuthObjectIdInAndCreator(
        scenarioId, creatorIds, true);

    // Save authorization
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  /**
   * Get the repository for scenario authorizations.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<ScenarioAuth, Long> getRepository() {
    return this.scenarioAuthRepo;
  }
}




