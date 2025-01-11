package cloud.xcan.sdf.core.angustester.application.cmd.mock.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.MOCK_SERVICE;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.MockServiceConverter.toMockServiceAuth;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.AUTH_CANCEL;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.AUTH_UPDATED;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockServiceAuthCmd;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.mock.MockServiceQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceInfo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockServiceRepo;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuth;
import cloud.xcan.sdf.core.angustester.domain.mock.service.auth.MockServiceAuthRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizAssert;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaolong.liu
 */
@Biz
@Slf4j
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
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
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

        // Add modify permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(MOCK_SERVICE, serviceDb, AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long serviceId, boolean enabledFlag) {
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
        mockServiceRepo.updateAuthFlagById(serviceId, enabledFlag);

        // Enable permission control activity
        activityCmd.add(toActivity(MOCK_SERVICE, serviceDb,
            enabledFlag ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

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
        BizAssert.assertTrue(!authDb.getCreatorFlag(), FORBID_AUTH_CREATOR_CODE,
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

  @Override
  public void addCreatorAuth(Set<Long> serviceIds) {
    List<MockServiceAuth> serviceAuths = new ArrayList<>();
    serviceIds.forEach(id -> serviceAuths.add(toMockServiceAuth(uidGenerator.getUID(), id)));
    batchInsert(serviceAuths, "authObjectId");
  }

  @Override
  protected BaseRepository<MockServiceAuth, Long> getRepository() {
    return this.mockServiceAuthRepo;
  }
}
