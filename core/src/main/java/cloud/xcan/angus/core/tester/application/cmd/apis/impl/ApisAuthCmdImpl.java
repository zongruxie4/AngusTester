package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR;
import static cloud.xcan.angus.api.commonlink.TesterApisMessage.FORBID_AUTH_CREATOR_CODE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisAuthConverter.toApisCreatorAuth;
import static cloud.xcan.angus.core.tester.application.converter.ServicesAuthConverter.toServicesViewPermission;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizAssert;
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
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

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

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisBasicInfo apiInfoDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the api exists
        apiInfoDb = apisQuery.checkAndFindBasicInfo(auth.getApisId());
        // Check the add creator permissions
        BizAssert.assertTrue(!apiInfoDb.getCreatedBy().equals(auth.getAuthObjectId()),
            FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);

        // Check the user have api authorization permissions
        apisAuthQuery.checkGrantAuth(getUserId(), auth.getApisId());
        // Check the authorization object exists
        authObjectName = commonQuery
            .checkAndGetAuthName(auth.getAuthObjectType(), auth.getAuthObjectId());
        // Check the duplicate authorizations
        apisAuthQuery.checkRepeatAuth(auth.getApisId(), auth.getAuthObjectId(),
            auth.getAuthObjectType());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = insert(auth, "authObjectId");

        // Determine and initialize parent project view permissions
        List<ServicesAuth> projectAuths = addParentViewPermission(auth);
        if (ObjectUtils.isNotEmpty(projectAuths)) {
          servicesAuthRepo.batchInsert0(projectAuths);
        }

        // Add grant permission activity
        if (Objects.nonNull(auth.getCreator()) && !auth.getCreator()) {
          activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH, authObjectName));
        }
        return idKey;
      }

      private List<ServicesAuth> addParentViewPermission(ApisAuth apisAuth) {
        List<ServicesAuth> auths = new ArrayList<>();
        long count = servicesAuthRepo
            .countByServiceIdAndAuthObjectIdAndAuthObjectType(apiInfoDb.getServiceId(),
                apisAuth.getAuthObjectId(), apisAuth.getAuthObjectType());
        if (count < 1) {
          auths.add(toServicesViewPermission(apiInfoDb.getServiceId(), apisAuth.getAuthObjectType(),
              apisAuth.getAuthObjectId()));
        }
        return auths;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void replace(ApisAuth auth) {
    new BizTemplate<Void>() {
      ApisAuth authDb;
      ApisBasicInfo apiInfoDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the Auth existed
        authDb = apisAuthQuery.checkAndFind(auth.getId());
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the api exists
        apiInfoDb = apisQuery.checkAndFindBasicInfo(authDb.getApisId());
        // Check the current user have api authorization permissions
        apisAuthQuery.checkGrantAuth(getUserId(), authDb.getApisId());
        // Check the authorization object exists
        authObjectName = commonQuery.checkAndGetAuthName(authDb.getAuthObjectType(),
            authDb.getAuthObjectId());
      }

      @Override
      protected Void process() {
        // Replace authorization
        authDb.setAuths(auth.getAuths());
        apisAuthRepo.save(authDb);

        // Add modification permission activity
        if (!authDb.isCreatorAuth()) {
          activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH_UPDATED, authObjectName));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      ApisAuth authDb;
      ApisBasicInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Check the api auth exists
        authDb = apisAuthQuery.checkAndFind(id);
        // Check the modify creator permissions
        BizAssert.assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE,
            FORBID_AUTH_CREATOR);
        // Check the api exists
        apiInfoDb = apisQuery.checkAndFindBasicInfo(authDb.getApisId());
        // Check the user have api authorization permissions
        apisAuthQuery.checkGrantAuth(getUserId(), authDb.getApisId());
      }

      @Override
      protected Void process() {
        // Get if authorization object name
        String authObjectName = "";
        try {
          authObjectName = commonQuery
              .checkAndGetAuthName(authDb.getAuthObjectType(), authDb.getAuthObjectId());
        } catch (Exception e) {
          // NOOP: Authorization can also be cancelled after the authorization object is deleted
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH_CANCEL, authObjectName));

        // Delete api permission
        apisAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void enabled(Long apisId, Boolean enabled) {
    new BizTemplate<Void>() {
      ApisBaseInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Check and get apis exists
        apiInfoDb = apisQuery.checkAndFindBaseInfo(apisId);

        // Check the to have permission to grant apis
        apisAuthQuery.checkGrantAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        apisRepo.updateAuthById(apisId, enabled);
        // Enable permission control activity
        activityCmd.add(toActivity(API, apiInfoDb,
            enabled ? ActivityType.AUTH_ENABLED : ActivityType.AUTH_DISABLED));
        return null;
      }
    }.execute();
  }

  @Override
  public void addCreatorAuth(Set<Long> apisIds, Set<Long> creatorIds) {
    // Allow modification of new authorization
    apisAuthRepo.deleteByApisIdInAndCreator(apisIds, true);

    // Save api creator authorization
    List<ApisAuth> apisAuths = new ArrayList<>();
    for (Long apisId : apisIds) {
      apisAuths.addAll(creatorIds.stream()
          .map(creatorId -> toApisCreatorAuth(apisId, creatorId, uidGenerator))
          .collect(Collectors.toList()));
    }
    batchInsert(apisAuths, "authObjectId");
  }

  @Override
  public void addCreatorAuth(Map<Long, Set<Long>> apisIdAndCreatorIds) {
    // Allow modification of new authorization
    apisAuthRepo.deleteByApisIdInAndCreator(apisIdAndCreatorIds.keySet(), true);

    // Save api creator authorization
    List<ApisAuth> apisAuths = new ArrayList<>();
    for (Long apisId : apisIdAndCreatorIds.keySet()) {
      apisAuths.addAll(apisIdAndCreatorIds.get(apisId).stream()
          .map(creatorId -> toApisCreatorAuth(apisId, creatorId, uidGenerator))
          .collect(Collectors.toList()));
    }
    batchInsert(apisAuths, "authObjectId");
  }

  @Override
  public void moveCreatorAuth(Services targetProjectDb, List<Long> apiIds, List<Apis> apis) {
    Set<ApisAuth> apisAuths = new HashSet<>();
    Set<Long> creatorIds = new HashSet<>();
    creatorIds.add(targetProjectDb.getCreatedBy());
    // Move from parent service to parent project exists in the apiIds
    apisAuthRepo.deleteByApisIdInAndAuthObjectIdInAndCreator(apiIds, creatorIds, true);
    for (Apis api : apis) {
      apisAuths.addAll(toApisCreatorAuth(api, creatorIds));
    }
    batchInsert0(apisAuths);
  }

  @Override
  protected BaseRepository<ApisAuth, Long> getRepository() {
    return this.apisAuthRepo;
  }
}
