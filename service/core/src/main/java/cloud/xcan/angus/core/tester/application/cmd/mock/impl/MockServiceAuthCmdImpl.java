package cloud.xcan.angus.core.tester.application.cmd.mock.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.MOCK_SERVICE;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.toMockServiceAuth;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_CANCEL;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.AUTH_UPDATED;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceAuthCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceRepo;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServiceAuthRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for mock service authorization management.
 * <p>
 * Provides methods for adding, replacing, enabling/disabling, and deleting mock service
 * authorizations.
 * <p>
 * Ensures permission checks, duplicate prevention, and activity logging.
 */
@Slf4j
@Biz
public class MockServiceAuthCmdImpl extends CommCmd<MockServiceAuth, Long> implements
    MockServiceAuthCmd {

  @Resource
  private MockServiceAuthRepo mockServiceAuthRepo;
  @Resource
  private MockServiceAuthQuery mockServiceAuthQuery;
  @Resource
  private MockServiceQuery mockServiceQuery;
  @Resource
  private MockServiceRepo mockServiceRepo;
  @Resource
  private CommonQuery commonQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a new authorization for a mock service.
   * <p>
   * Checks service existence, permission, and duplicate authorization before adding.
   * <p>
   * Logs grant permission activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(MockServiceAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      MockServiceInfo serviceDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = mockServiceQuery.checkAndFindInfo(auth.getMockServiceId());
        // Check the add creator permissions
        BizAssert.assertTrue(!serviceDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);

        // Check the current user have service authorization permissions
        mockServiceAuthQuery.checkGrantAuth(getUserId(), auth.getMockServiceId());

        // Check the authorization object exists
        authObjectName = commonQuery
            .checkAndGetAuthName(auth.getAuthObjectType(), auth.getAuthObjectId());

        // Check the for duplicate authorizations
        mockServiceAuthQuery.checkRepeatAuth(auth.getMockServiceId(), auth.getAuthObjectId(),
            auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Add grant permission activity
        if (!auth.isCreatorAuth()) {
          activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, ActivityType.AUTH, authObjectName));
        }
        return insert(auth, "authObjectId");
      }
    }.execute();
  }

  /**
   * Replace (update) an existing authorization for a mock service.
   * <p>
   * Checks existence, permission, and updates authorization details.
   * <p>
   * Logs modification activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(MockServiceAuth serviceAuth) {
    new BizTemplate<Void>() {
      MockServiceAuth authDb;
      MockServiceInfo serviceDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the Auth existed
        authDb = mockServiceAuthQuery.checkAndFind(serviceAuth.getId());
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the service existed
        serviceDb = mockServiceQuery.checkAndFindInfo(authDb.getMockServiceId());
        // Check the current user have service authorization permissions
        mockServiceAuthQuery.checkGrantAuth(getUserId(), authDb.getMockServiceId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Replace authorization
        authDb.setAuths(serviceAuth.getAuths());
        mockServiceAuthRepo.save(authDb);

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Enable or disable authorization control for a mock service.
   * <p>
   * Checks existence and permission before updating authorization status.
   * <p>
   * Logs enable/disable activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long serviceId, boolean enabled) {
    new BizTemplate<Void>() {
      MockServiceInfo serviceDb;

      @Override
      protected void checkParams() {
        // Check the service exists
        serviceDb = mockServiceQuery.checkAndFindInfo(serviceId);
        // Check the current user have service authorization permissions
        mockServiceAuthQuery.checkGrantAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        mockServiceRepo.updateAuthById(serviceId, enabled);

        // Enable permission control activity
        activityCmd.add(toActivity(MOCK_SERVICE, serviceDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  /**
   * Delete an authorization for a mock service.
   * <p>
   * Checks existence and permission before deleting authorization.
   * <p>
   * Logs cancel activity and deletes the authorization.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      MockServiceAuth authDb;
      MockServiceInfo serviceDb;

      @Override
      protected void checkParams() {
        // Check the service auth exists
        authDb = mockServiceAuthQuery.checkAndFind(id);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);

        // Check the current user have service authorization permissions
        mockServiceAuthQuery.checkGrantAuth(getUserId(), authDb.getMockServiceId());

        // Check the service exists
        serviceDb = mockServiceQuery.checkAndFindInfo(authDb.getMockServiceId());
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
        activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, AUTH_CANCEL, authObjectName));

        // Delete service permission
        mockServiceAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  /**
   * Add creator authorization for a batch of mock services.
   * <p>
   * Batch inserts creator authorizations for the specified services.
   */
  @Override
  public void addCreatorAuth(Set<Long> serviceIds) {
    List<MockServiceAuth> serviceAuths = new ArrayList<>();
    serviceIds.forEach(id -> serviceAuths.add(toMockServiceAuth(uidGenerator.getUID(), id)));
    batchInsert(serviceAuths, "authObjectId");
  }

  /**
   * Get the repository for mock service authorizations.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<MockServiceAuth, Long> getRepository() {
    return this.mockServiceAuthRepo;
  }
}
