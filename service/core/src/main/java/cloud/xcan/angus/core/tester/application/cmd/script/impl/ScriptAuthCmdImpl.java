package cloud.xcan.angus.core.tester.application.cmd.script.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCRIPT;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ScriptAuthConverter.toScriptCreatorAuths;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptAuthQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.script.ScriptRepo;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuth;
import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of script authorization command operations.
 * 
 * <p>This class provides comprehensive functionality for managing script authorizations,
 * including adding, replacing, deleting, and enabling/disabling authorization controls.</p>
 * 
 * <p>It handles both creator and regular user authorizations with proper permission
 * validation and activity logging for audit purposes.</p>
 * 
 * <p>The implementation uses the BizTemplate pattern to ensure consistent business
 * logic execution with proper parameter validation and transaction management.</p>
 */
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

  /**
   * Adds a new script authorization for a user or group.
   * 
   * <p>This method performs comprehensive validation including checking script existence,
   * preventing creator self-authorization, verifying user permissions, and ensuring
   * no duplicate authorizations exist.</p>
   * 
   * <p>For non-creator authorizations, it also logs the authorization activity
   * for audit purposes.</p>
   * 
   * @param auth the script authorization to add
   * @return the ID key of the created authorization
   * @throws IllegalArgumentException if validation fails
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ScriptAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ScriptInfo scriptDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify the script exists and retrieve its information
        scriptDb = scriptQuery.checkAndFindInfo(auth.getScriptId());
        
        // Prevent creator from authorizing themselves
        BizAssert.assertTrue(!scriptDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        
        // Verify current user has authorization grant permissions for this script
        scriptAuthQuery.checkGrantAuth(getUserId(), auth.getScriptId());
        
        // Verify the authorization object (user/group) exists and get its name
        authObjectName = commonQuery.checkAndGetAuthName(auth.getAuthObjectType(),
            auth.getAuthObjectId());
        
        // Check for duplicate authorizations to prevent conflicts
        scriptAuthQuery.checkRepeatAuth(auth.getScriptId(),
            auth.getAuthObjectId(), auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Log authorization activity for non-creator authorizations
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH, authObjectName));
        }

        return insert(auth, "authObjectId");
      }
    }.execute();
  }

  /**
   * Replaces the permissions of an existing script authorization.
   * 
   * <p>This method updates the authorization permissions while maintaining the same
   * authorization object and script relationship. It validates that the authorization
   * exists, is not a creator authorization, and that the current user has proper
   * permissions to modify it.</p>
   * 
   * <p>The method logs the modification activity for audit tracking.</p>
   * 
   * @param auth the authorization with updated permissions
   * @throws IllegalArgumentException if validation fails or authorization not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(ScriptAuth auth) {
    new BizTemplate<Void>() {
      ScriptAuth authDb;
      ScriptInfo scriptDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify the authorization exists and retrieve it
        authDb = scriptAuthQuery.checkAndFind(auth.getId());
        
        // Verify the associated script exists
        scriptDb = scriptQuery.checkAndFindInfo(authDb.getScriptId());
        
        // Prevent modification of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        
        // Verify current user has authorization grant permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), authDb.getScriptId());
        
        // Get the authorization object name for activity logging
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Update the authorization permissions
        authDb.setAuths(auth.getAuths());
        scriptAuthRepo.save(authDb);

        // Log the authorization modification activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Deletes a script authorization by its ID.
   * 
   * <p>This method removes an authorization while performing proper validation
   * and logging. It handles cases where the authorization object may have been
   * deleted, ensuring graceful degradation.</p>
   * 
   * <p>The method logs the cancellation activity before deletion to maintain
   * audit trail integrity.</p>
   * 
   * @param authId the ID of the authorization to delete
   * @throws IllegalArgumentException if validation fails or authorization not found
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long authId) {
    new BizTemplate<Void>() {
      ScriptAuth authDb;
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Verify the authorization exists
        authDb = scriptAuthQuery.checkAndFind(authId);
        
        // Prevent deletion of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        
        // Verify the associated script exists
        scriptDb = scriptQuery.checkAndFindInfo(authDb.getScriptId());
        
        // Verify current user has authorization grant permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), authDb.getScriptId());
      }

      @Override
      protected Void process() {
        // Attempt to get authorization object name for activity logging
        String authObjectName = "";
        try {
          authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
              authDb.getAuthObjectId());
        } catch (Exception e) {
          // Gracefully handle cases where authorization object has been deleted
          // Authorization can still be cancelled even if the object no longer exists
        }

        // Log the authorization cancellation activity before deletion
        activityCmd.add(toActivity(SCRIPT, scriptDb, ActivityType.AUTH_CANCEL, authObjectName));

        // Delete the authorization
        scriptAuthRepo.deleteById(authId);

        return null;
      }
    }.execute();
  }

  /**
   * Enables or disables authorization control for a script.
   * 
   * <p>This method controls whether authorization checks are enforced for a script.
   * When disabled, the script becomes accessible without authorization validation.</p>
   * 
   * <p>The method logs the enable/disable activity for audit purposes.</p>
   * 
   * @param scriptId the ID of the script to modify authorization control
   * @param enabled true to enable authorization control, false to disable
   * @throws IllegalArgumentException if script not found or user lacks permissions
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long scriptId, Boolean enabled) {
    new BizTemplate<Void>() {
      ScriptInfo scriptDb;

      @Override
      protected void checkParams() {
        // Verify the script exists
        scriptDb = scriptQuery.checkAndFindInfo(scriptId);
        
        // Verify current user has authorization grant permissions
        scriptAuthQuery.checkGrantAuth(getUserId(), scriptId);
      }

      @Override
      protected Void process() {
        // Update the authorization control setting
        scriptRepo.updateAuthById(scriptId, enabled);

        // Log the authorization control change activity
        activityCmd.add(toActivity(SCRIPT, scriptDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Adds creator authorizations for multiple users to a script.
   * 
   * <p>This method replaces any existing creator authorizations with the new set
   * of creator IDs. It first removes all existing creator authorizations for the
   * script, then adds the new ones.</p>
   * 
   * @param creatorIds collection of user IDs to grant creator permissions
   * @param scriptId the ID of the script to modify
   */
  @Override
  public void addCreatorAuth(Collection<Long> creatorIds, Long scriptId) {
    // Remove existing creator authorizations to avoid conflicts
    scriptAuthRepo.deleteByScriptIdAndCreator(scriptId, true);

    // Add new creator authorizations
    batchInsert(toScriptCreatorAuths(creatorIds, scriptId, uidGenerator), "authObjectId");
  }

  /**
   * Moves creator authorizations for specific users to a script.
   * 
   * <p>This method is used when scripts are moved between directories or projects.
   * It removes creator authorizations for the specified users from the current script
   * and adds them to the target script.</p>
   * 
   * @param creatorIds collection of user IDs whose creator permissions should be moved
   * @param scriptId the ID of the target script
   */
  @Override
  public void moveCreatorAuth(Collection<Long> creatorIds, Long scriptId) {
    // Remove creator authorizations for specified users from current script
    scriptAuthRepo.deleteByScriptIdAndAuthObjectIdInAndCreator(scriptId, creatorIds, true);

    // Add creator authorizations to the target script
    batchInsert(toScriptCreatorAuths(creatorIds, scriptId, uidGenerator), "authObjectId");
  }

  /**
   * Deletes all authorizations for multiple scripts.
   * 
   * <p>This method is typically used during bulk operations such as script deletion
   * or project cleanup to remove all associated authorizations.</p>
   * 
   * @param scriptIds collection of script IDs whose authorizations should be deleted
   */
  @Override
  public void deleteByScriptIdIn(Collection<Long> scriptIds) {
    scriptAuthRepo.deleteByScriptIdIn(scriptIds);
  }

  /**
   * Returns the repository instance for this command.
   * 
   * @return the script authorization repository
   */
  @Override
  protected BaseRepository<ScriptAuth, Long> getRepository() {
    return this.scriptAuthRepo;
  }
}




