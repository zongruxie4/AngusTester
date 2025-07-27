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
 * Command implementation for scenario authorization management operations.
 * <p>
 * Provides comprehensive authorization operations for scenarios including creation, 
 * modification, deletion, and status management.
 * <p>
 * Implements business logic validation, permission checks, duplicate prevention, 
 * and activity logging for all authorization operations.
 * <p>
 * Supports creator authorization management, authorization control enablement, 
 * and comprehensive activity tracking.
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
   * Adds a new authorization for a scenario.
   * <p>
   * Performs comprehensive validation including scenario existence, user permissions, 
   * creator authorization restrictions, and duplicate prevention.
   * <p>
   * Prevents creators from being granted additional authorizations and logs 
   * authorization grant activity for audit trail purposes.
   * <p>
   * Ensures proper authorization setup and validation.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScenarioAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Scenario scenarioDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind0(auth.getScenarioId());
        
        // Prevent creators from being granted additional authorizations
        BizAssert.assertTrue(!scenarioDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        
        // Check user permission to grant scenario authorizations
        scenarioAuthQuery.checkGrantAuth(getUserId(), auth.getScenarioId());
        
        // Ensure the authorization object exists and get its name
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        
        // Prevent duplicate authorizations for the same object
        scenarioAuthQuery.checkRepeatAuth(auth.getScenarioId(),
            auth.getAuthObjectId(), auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Log authorization grant activity (skip for creator auths)
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(SCENARIO, scenarioDb,
              ActivityType.AUTH, authObjectName));
        }
        
        // Save authorization to database
        return insert(auth, "authObjectId");
      }
    }.execute();
  }

  /**
   * Replaces (updates) an existing authorization for a scenario.
   * <p>
   * Validates authorization existence, user permissions, and creator restrictions 
   * before updating authorization details.
   * <p>
   * Prevents modification of creator authorizations and logs modification activity 
   * for audit trail purposes.
   * <p>
   * Updates authorization permissions while maintaining audit trail.
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
        // Ensure the authorization exists in database
        authDb = scenarioAuthQuery.checkAndFind(auth.getId());
        
        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind0(authDb.getScenarioId());
        
        // Prevent modification of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        
        // Check user permission to modify scenario authorizations
        scenarioAuthQuery.checkGrantAuth(getUserId(), authDb.getScenarioId());
        
        // Ensure the authorization object exists and get its name
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Update authorization permissions
        authDb.setAuths(auth.getAuths());
        scenarioAuthRepo.save(authDb);

        // Log authorization modification activity (skip for creator auths)
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SCENARIO, scenarioDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Deletes an authorization for a scenario.
   * <p>
   * Validates authorization existence, user permissions, and creator restrictions 
   * before removing the authorization.
   * <p>
   * Prevents deletion of creator authorizations and logs cancellation activity 
   * for audit trail purposes.
   * <p>
   * Handles cases where authorization objects may have been deleted.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long scenarioId) {
    new BizTemplate<Void>() {
      ScenarioAuth authDb;
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Ensure the authorization exists in database
        authDb = scenarioAuthQuery.checkAndFind(scenarioId);
        
        // Prevent deletion of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        
        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind0(authDb.getScenarioId());
        
        // Check user permission to delete scenario authorizations
        scenarioAuthQuery.checkGrantAuth(getUserId(), authDb.getScenarioId());
      }

      @Override
      protected Void process() {
        // Get authorization object name for activity logging
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // Authorization can be cancelled even if the object was deleted
        }

        // Log authorization cancellation activity (must be done before deletion)
        activityCmd.add(toActivity(SCENARIO, scenarioDb, AUTH_CANCEL, authObjectName));

        // Permanently delete the authorization from database
        scenarioAuthRepo.deleteById(scenarioId);

        return null;
      }
    }.execute();
  }

  /**
   * Enables or disables authorization control for a scenario.
   * <p>
   * Validates scenario existence and user permissions before updating 
   * authorization control status.
   * <p>
   * Controls whether authorization checks are enforced for the scenario.
   * <p>
   * Logs enable/disable activity for audit trail purposes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long id, Boolean enabled) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Ensure the scenario exists in database
        scenarioDb = scenarioQuery.checkAndFind0(id);
        
        // Check user permission to manage scenario authorizations
        scenarioAuthQuery.checkGrantAuth(getUserId(), id);
      }

      @Override
      protected Void process() {
        // Update authorization control status in database
        scenarioRepo.updateAuthById(id, enabled);

        // Log authorization control status change activity
        activityCmd.add(toActivity(SCENARIO, scenarioDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Adds creator authorizations for a scenario.
   * <p>
   * Removes existing creator authorizations and creates new ones for the specified creators.
   * <p>
   * Ensures only the specified creators have creator-level permissions for the scenario.
   * <p>
   * This is a batch operation for managing creator access.
   */
  @Override
  public void addCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Remove existing creator authorizations to avoid duplicates
    scenarioAuthRepo.deleteByScenarioIdAndCreator(scenarioId, true);

    // Create new creator authorizations for specified users
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  /**
   * Moves creator authorizations for a scenario.
   * <p>
   * Removes creator authorizations for specific users and creates new ones.
   * <p>
   * Used when scenarios are moved between directories to update creator permissions.
   * <p>
   * This is a targeted operation for specific creator transfers.
   */
  @Override
  public void moveCreatorAuth(Set<Long> creatorIds, Long scenarioId) {
    // Remove creator authorizations for specific users (scenario move scenario)
    scenarioAuthRepo.deleteByScenarioIdAndAuthObjectIdInAndCreator(
        scenarioId, creatorIds, true);

    // Create new creator authorizations for the moved users
    batchInsert(toScenarioCreatorAuths(creatorIds, scenarioId, uidGenerator), "authObjectId");
  }

  /**
   * Gets the repository for scenario authorization entities.
   * <p>
   * Used by the base command class for generic operations.
   * <p>
   * Provides access to the underlying scenario authorization data store.
   */
  @Override
  protected BaseRepository<ScenarioAuth, Long> getRepository() {
    return this.scenarioAuthRepo;
  }
}




