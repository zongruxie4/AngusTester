package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ServicesAuthConverter.toServicesCreatorAuth;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesRepo;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuth;
import cloud.xcan.angus.core.tester.domain.services.auth.ServicesAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of service authorization command operations.
 *
 * <p>This class provides comprehensive functionality for managing service authorizations,
 * including adding, replacing, deleting, and enabling/disabling authorization controls.</p>
 *
 * <p>It handles both creator and regular user authorizations with proper permission
 * validation and activity logging for audit purposes.</p>
 *
 * <p>The implementation uses the BizTemplate pattern to ensure consistent business
 * logic execution with proper parameter validation and transaction management.</p>
 */
@Biz
public class ServicesAuthCmdImpl extends CommCmd<ServicesAuth, Long> implements ServicesAuthCmd {

  @Resource
  private ServicesAuthRepo servicesAuthRepo;
  @Resource
  private ServicesAuthQuery servicesAuthQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ServicesRepo servicesRepo;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Adds a new service authorization for a user or group.
   *
   * <p>This method performs comprehensive validation including checking service existence,
   * preventing creator self-authorization, verifying user permissions, and ensuring
   * no duplicate authorizations exist.</p>
   *
   * <p>For non-creator authorizations, it also logs the authorization activity
   * for audit purposes.</p>
   *
   * @param auth the service authorization to add
   * @return the ID key of the created authorization
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  public IdKey<Long, Object> add(ServicesAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Services serviceDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify the service exists and retrieve it
        serviceDb = servicesQuery.checkAndFind(auth.getServiceId());

        // Prevent creator from authorizing themselves
        BizAssert.assertTrue(!serviceDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);

        // Verify current user has authorization grant permissions for this service
        servicesAuthQuery.checkGrantAuth(getUserId(), auth.getServiceId());

        // Verify the authorization object (user/group) exists and get its name
        authObjectName = commonQuery
            .checkAndGetAuthName(auth.getAuthObjectType(), auth.getAuthObjectId());

        // Check for duplicate authorizations to prevent conflicts
        servicesAuthQuery.checkRepeatAuth(auth.getServiceId(), auth.getAuthObjectId(),
            auth.getAuthObjectType(), false);
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Log authorization activity for non-creator authorizations
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.AUTH, authObjectName));
        }

        return batchInsert(List.of(auth), "authObjectId").get(0);
      }
    }.execute();
  }

  /**
   * Replaces the permissions of an existing service authorization.
   *
   * <p>This method updates the authorization permissions while maintaining the same
   * authorization object and service relationship. It validates that the authorization
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
  public void replace(ServicesAuth auth) {
    new BizTemplate<Void>() {
      ServicesAuth authDb;
      Services serviceDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Verify the authorization exists and retrieve it
        authDb = servicesAuthQuery.checkAndFind(auth.getId());

        // Prevent modification of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);

        // Verify the associated service exists
        serviceDb = servicesQuery.checkAndFind(authDb.getServiceId());

        // Verify current user has authorization grant permissions
        servicesAuthQuery.checkGrantAuth(getUserId(), authDb.getServiceId());

        // Get the authorization object name for activity logging
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Update the authorization permissions
        authDb.setAuths(auth.getAuths());
        servicesAuthRepo.save(authDb);

        // Log the authorization modification activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.AUTH_UPDATED,
              authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Deletes a service authorization by its ID.
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
      ServicesAuth authDb;
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify the authorization exists
        authDb = servicesAuthQuery.checkAndFind(authId);

        // Prevent deletion of creator authorizations
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);

        // Verify the associated service exists
        serviceDb = servicesQuery.checkAndFind(authDb.getServiceId());

        // Verify current user has authorization grant permissions
        servicesAuthQuery.checkGrantAuth(getUserId(), authDb.getServiceId());
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
        if (Objects.nonNull(authId)) {
          activityCmd.add(toActivity(SERVICE, serviceDb, ActivityType.AUTH_CANCEL, authObjectName));
        }

        // Delete the authorization
        servicesAuthRepo.deleteById(authId);
        return null;
      }
    }.execute();
  }

  /**
   * Enables or disables authorization control for a service.
   *
   * <p>This method controls whether authorization checks are enforced for a service.
   * When disabled, the service becomes accessible without authorization validation.</p>
   *
   * <p>The method also updates the authorization status of all associated APIs
   * and logs the enable/disable activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service to modify authorization control
   * @param enabled true to enable authorization control, false to disable
   * @throws IllegalArgumentException if service not found or user lacks permissions
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long serviceId, Boolean enabled) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Verify current user has authorization grant permissions
        servicesAuthQuery.checkGrantAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        // Update service authorization control setting
        servicesRepo.updateAuthById(serviceId, enabled);

        // Update authorization status for all associated APIs
        apisRepo.updateServiceAuthByServiceId(serviceId, enabled);

        // Log the authorization control change activity
        activityCmd.add(toActivity(SERVICE, serviceDb, enabled
            ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Enables or disables authorization control for APIs within a service.
   *
   * <p>This method specifically controls authorization for APIs within a service
   * without affecting the service-level authorization control.</p>
   *
   * <p>The method logs the API authorization control change activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service whose APIs should be modified
   * @param enabled true to enable API authorization control, false to disable
   * @throws IllegalArgumentException if service not found or user lacks permissions
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void apisEnabled(Long serviceId, Boolean enabled) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify the service exists
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Verify current user has authorization grant permissions
        servicesAuthQuery.checkGrantAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        // Update authorization status for all APIs within the service
        apisRepo.updateServiceAuthByServiceId(serviceId, enabled);

        // Log the API authorization control change activity
        activityCmd.add(toActivity(CombinedTargetType.API, serviceDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Adds creator authorizations for multiple users to a service.
   *
   * <p>This method replaces any existing creator authorizations with the new set
   * of creator IDs. It first removes all existing creator authorizations for the
   * service, then adds the new ones.</p>
   *
   * @param serviceId the ID of the service to modify
   * @param creatorIds set of user IDs to grant creator permissions
   */
  @Override
  public void addCreatorAuth(Long serviceId, Set<Long> creatorIds) {
    // Remove existing creator authorizations to avoid conflicts
    servicesAuthRepo.deleteByServiceIdAndCreator(serviceId, true);

    // Add new creator authorizations
    List<ServicesAuth> serviceAuths = creatorIds.stream()
        .map(creatorId -> toServicesCreatorAuth(creatorId, serviceId, uidGenerator))
        .toList();
    batchInsert(serviceAuths, "authObjectId");
  }

  /**
   * Moves creator authorization for a specific user to a service.
   *
   * <p>This method is used when services are moved between projects or when
   * creator permissions need to be transferred. It removes the creator authorization
   * for the specified user from the current service and adds it to the target service.</p>
   *
   * @param serviceId the ID of the target service
   * @param creatorId the ID of the user whose creator permissions should be moved
   */
  @Override
  public void moveCreatorAuth(Long serviceId, Long creatorId) {
    // Remove creator authorization for the specified user from current service
    servicesAuthRepo.deleteByServiceIdAndAuthObjectIdAndCreator(serviceId, creatorId, true);

    // Add creator authorization to the target service
    insert(toServicesCreatorAuth(creatorId, serviceId, uidGenerator), "authObjectId");
  }

  /**
   * Deletes all authorizations for multiple services.
   *
   * <p>This method is typically used during bulk operations such as service deletion
   * or project cleanup to remove all associated authorizations efficiently.</p>
   *
   * @param serviceIds collection of service IDs whose authorizations should be deleted
   */
  @Override
  public void deleteAllByProject(Collection<Long> serviceIds) {
    servicesAuthRepo.deleteByServiceIdIn(serviceIds);
  }

  /**
   * Returns the repository instance for this command.
   *
   * @return the service authorization repository
   */
  @Override
  protected BaseRepository<ServicesAuth, Long> getRepository() {
    return this.servicesAuthRepo;
  }
}




