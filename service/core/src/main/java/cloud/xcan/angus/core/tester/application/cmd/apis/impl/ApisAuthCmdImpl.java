package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.biz.BizAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisAuthConverter.toApisCreatorAuth;
import static cloud.xcan.angus.core.tester.application.converter.ServicesAuthConverter.toServicesViewPermission;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisAuthCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisBasicInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuth;
import cloud.xcan.angus.core.tester.domain.apis.auth.ApisAuthRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of ApisAuthCmd for API authorization management and command operations.
 * </p>
 * <p>
 * Provides comprehensive authorization management services including adding, replacing, deleting,
 * enabling/disabling, and batch operations on API authorizations. Handles permission validation,
 * creator authorization management, and activity logging for all authorization operations.
 * </p>
 */
@Slf4j
@Biz
public class ApisAuthCmdImpl extends CommCmd<ApisAuth, Long> implements ApisAuthCmd {

  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ApisAuthRepo apisAuthRepo;
  @Resource
  private ServicesAuthRepo servicesAuthRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Add API authorization with comprehensive validation and permission management.
   * </p>
   * <p>
   * Validates API existence, creator permissions, user authorization rights, authorization object
   * existence, and duplicate authorization checks. Inserts authorization record, initializes parent
   * project view permissions, and logs grant permission activity.
   * </p>
   * @param auth API authorization to add
   * @return ID key of the created authorization
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisBasicInfo apiInfoDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Validate API exists and retrieve basic information
        apiInfoDb = apisQuery.checkAndFindBasicInfo(auth.getApisId());
        
        // Prevent creator from authorizing themselves (security check)
        assertTrue(!apiInfoDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);

        // Verify current user has permission to grant API authorization
        apisAuthQuery.checkGrantAuth(getUserId(), auth.getApisId());
        
        // Validate authorization object exists and get its name for activity logging
        authObjectName = commonQuery
            .checkAndGetAuthName(auth.getAuthObjectType(), auth.getAuthObjectId());
        
        // Prevent duplicate authorization assignments
        apisAuthQuery.checkRepeatAuth(auth.getApisId(), auth.getAuthObjectId(),
            auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Insert the authorization record
        IdKey<Long, Object> idKey = insert(auth, "authObjectId");

        // Initialize parent project view permissions if needed
        List<ServicesAuth> projectAuths = addParentViewPermission(auth);
        if (isNotEmpty(projectAuths)) {
          servicesAuthRepo.batchInsert0(projectAuths);
        }

        // Log grant permission activity (skip for creator authorizations)
        if (nonNull(auth.getCreator()) && !auth.getCreator()) {
          activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH, authObjectName));
        }
        return idKey;
      }

      /**
       * <p>
       * Add parent project view permission for the authorization object.
       * </p>
       * <p>
       * Ensures that the authorization object has view permission to the parent service
       * if they don't already have it. This maintains proper permission hierarchy.
       * </p>
       * @param apisAuth API authorization being added
       * @return List of service authorizations to insert
       */
      private List<ServicesAuth> addParentViewPermission(ApisAuth apisAuth) {
        List<ServicesAuth> auths = new ArrayList<>();
        
        // Check if authorization object already has view permission to the service
        long count = servicesAuthRepo
            .countByServiceIdAndAuthObjectIdAndAuthObjectType(apiInfoDb.getServiceId(),
                apisAuth.getAuthObjectId(), apisAuth.getAuthObjectType());
        
        // Add view permission if not already present
        if (count < 1) {
          auths.add(toServicesViewPermission(apiInfoDb.getServiceId(), apisAuth.getAuthObjectType(),
              apisAuth.getAuthObjectId()));
        }
        return auths;
      }
    }.execute();
  }

  /**
   * <p>
   * Replace API authorization with comprehensive validation and permission management.
   * </p>
   * <p>
   * Validates authorization existence, creator permissions, API existence, user authorization rights,
   * and authorization object existence. Updates authorization permissions and logs modification activity.
   * </p>
   * @param auth API authorization to replace
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(ApisAuth auth) {
    new BizTemplate<Void>() {
      ApisAuth authDb;
      ApisBasicInfo apiInfoDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Validate authorization exists and retrieve current record
        authDb = apisAuthQuery.checkAndFind(auth.getId());
        
        // Prevent modification of creator authorizations (security check)
        assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        
        // Validate API exists and retrieve basic information
        apiInfoDb = apisQuery.checkAndFindBasicInfo(authDb.getApisId());
        
        // Verify current user has permission to grant API authorization
        apisAuthQuery.checkGrantAuth(getUserId(), authDb.getApisId());
        
        // Validate authorization object exists and get its name for activity logging
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Update authorization permissions
        authDb.setAuths(auth.getAuths());
        apisAuthRepo.save(authDb);

        // Log modification permission activity (skip for creator authorizations)
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Delete API authorization with comprehensive validation and activity logging.
   * </p>
   * <p>
   * Validates authorization existence, creator permissions, API existence, and user authorization rights.
   * Logs cancellation activity and removes the authorization record.
   * </p>
   * @param id Authorization ID to delete
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      ApisAuth authDb;
      ApisBasicInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Validate authorization exists and retrieve current record
        authDb = apisAuthQuery.checkAndFind(id);
        
        // Prevent deletion of creator authorizations (security check)
        assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        
        // Validate API exists and retrieve basic information
        apiInfoDb = apisQuery.checkAndFindBasicInfo(authDb.getApisId());
        
        // Verify current user has permission to grant API authorization
        apisAuthQuery.checkGrantAuth(getUserId(), authDb.getApisId());
      }

      @Override
      protected Void process() {
        // Retrieve authorization object name for activity logging (with error handling)
        String authObjectName = "";
        try {
          authObjectName = commonQuery
              .checkAndGetAuthName(authDb.getAuthObjectType(), authDb.getAuthObjectId());
        } catch (Exception e) {
          // Log the exception for troubleshooting but continue with deletion
          log.warn("Failed to get auth object name", e);
        }

        // Log cancellation activity before deletion (for audit trail)
        activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH_CANCEL, authObjectName));

        // Remove the authorization record
        apisAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Enable or disable API authorization control.
   * </p>
   * <p>
   * Validates API existence and user authorization rights. Updates authorization control status
   * and logs the enable/disable activity.
   * </p>
   * @param apisId API ID
   * @param enabled Whether to enable authorization control
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long apisId, Boolean enabled) {
    new BizTemplate<Void>() {
      ApisBaseInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Validate API exists and retrieve base information
        apiInfoDb = apisQuery.checkAndFindBaseInfo(apisId);

        // Verify current user has permission to grant API authorization
        apisAuthQuery.checkGrantAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Update authorization control status in API record
        apisRepo.updateAuthById(apisId, enabled);
        
        // Log enable/disable authorization control activity
        activityCmd.add(toActivity(API, apiInfoDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Batch add creator authorization for APIs.
   * </p>
   * <p>
   * Removes existing creator authorizations and inserts new ones for the specified APIs and creators.
   * This method is used for bulk creator permission management.
   * </p>
   * @param apisIds Set of API IDs
   * @param creatorIds Set of creator user IDs
   */
  @Override
  public void addCreatorAuth(Set<Long> apisIds, Set<Long> creatorIds) {
    // Remove existing creator authorizations for the specified APIs
    apisAuthRepo.deleteByApisIdInAndCreator(apisIds, true);
    
    // Create new creator authorization records for all API-creator combinations
    List<ApisAuth> apisAuths = apisIds.stream()
        .flatMap(apisId -> creatorIds.stream()
            .map(creatorId -> toApisCreatorAuth(apisId, creatorId, uidGenerator)))
        .toList();
    
    // Batch insert all new creator authorizations
    batchInsert(apisAuths, "authObjectId");
  }

  /**
   * <p>
   * Batch add creator authorization for APIs with mapping.
   * </p>
   * <p>
   * Removes existing creator authorizations and inserts new ones based on the provided mapping.
   * Each API can have different sets of creators, allowing for more granular permission management.
   * </p>
   * @param apisIdAndCreatorIds Mapping of API IDs to their respective creator user IDs
   */
  @Override
  public void addCreatorAuth(Map<Long, Set<Long>> apisIdAndCreatorIds) {
    // Remove existing creator authorizations for all specified APIs
    apisAuthRepo.deleteByApisIdInAndCreator(apisIdAndCreatorIds.keySet(), true);
    
    // Create new creator authorization records based on the mapping
    List<ApisAuth> apisAuths = apisIdAndCreatorIds.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(creatorId -> toApisCreatorAuth(entry.getKey(), creatorId, uidGenerator)))
        .toList();
    
    // Batch insert all new creator authorizations
    batchInsert(apisAuths, "authObjectId");
  }

  /**
   * <p>
   * Move creator authorization to a new project.
   * </p>
   * <p>
   * Removes existing creator authorizations from parent service and creates new ones for the target
   * project. This is used when APIs are moved between projects to maintain proper creator permissions.
   * </p>
   * @param targetProjectDb Target project entity
   * @param apiIds List of API IDs to move
   * @param apis List of API entities
   */
  @Override
  public void moveCreatorAuth(Services targetProjectDb, List<Long> apiIds, List<Apis> apis) {
    Set<ApisAuth> apisAuths = new HashSet<>();
    Set<Long> creatorIds = new HashSet<>();
    
    // Add target project creator to the creator set
    creatorIds.add(targetProjectDb.getCreatedBy());
    
    // Remove existing creator authorizations from parent service for the specified APIs
    apisAuthRepo.deleteByApisIdInAndAuthObjectIdInAndCreator(apiIds, creatorIds, true);
    
    // Create new creator authorizations for each API with the target project creator
    for (Apis api : apis) {
      apisAuths.addAll(toApisCreatorAuth(api, creatorIds));
    }
    
    // Batch insert all new creator authorizations
    batchInsert0(apisAuths);
  }

  /**
   * Get the repository for ApisAuth entity.
   * <p>
   *
   * @return the ApisAuthRepo instance
   */
  @Override
  protected BaseRepository<ApisAuth, Long> getRepository() {
    return this.apisAuthRepo;
  }
}
