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
   * Add API authorization.
   * <p>
   * Checks API, creator, permission, object, and duplication, then inserts and records activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisAuth auth) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisBasicInfo apiInfoDb;
      String authObjectName;

      @Override
      protected void checkParams() {
        // Check the api existed
        apiInfoDb = apisQuery.checkAndFindBasicInfo(auth.getApisId());
        // Check the add creator permissions
        assertTrue(!apiInfoDb.getCreatedBy().equals(auth.getAuthObjectId()),
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
        if (isNotEmpty(projectAuths)) {
          servicesAuthRepo.batchInsert0(projectAuths);
        }

        // Add grant permission activity
        if (nonNull(auth.getCreator()) && !auth.getCreator()) {
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

  /**
   * Replace API authorization.
   * <p>
   * Checks existence, creator, API, permission, and object, then updates and records activity.
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
        // Check the Auth existed
        authDb = apisAuthQuery.checkAndFind(auth.getId());
        // Check the modify creator permissions
        assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the api existed
        apiInfoDb = apisQuery.checkAndFindBasicInfo(authDb.getApisId());
        // Check the current user have api authorization permissions
        apisAuthQuery.checkGrantAuth(getUserId(), authDb.getApisId());
        // Check the authorization object existed
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

  /**
   * Delete API authorization.
   * <p>
   * Checks existence, creator, API, and permission, records cancel activity, then deletes.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      ApisAuth authDb;
      ApisBasicInfo apiInfoDb;

      @Override
      protected void checkParams() {
        // Check the api auth existed
        authDb = apisAuthQuery.checkAndFind(id);
        // Check the modify creator permissions
        assertTrue(!authDb.getCreator(), FORBID_AUTH_CREATOR_CODE, FORBID_AUTH_CREATOR);
        // Check the api existed
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
          // Log the exception for troubleshooting.
          log.warn("Failed to get auth object name", e);
        }

        // Add deleted permission activity, must be deleted before
        activityCmd.add(toActivity(API, apiInfoDb, ActivityType.AUTH_CANCEL, authObjectName));

        // Delete api permission
        apisAuthRepo.deleteById(id);
        return null;
      }
    }.execute();
  }

  /**
   * Enable or disable API authorization.
   * <p>
   * Checks API and permission, updates status, and records activity.
   */
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

  /**
   * Batch add creator authorization for APIs.
   * <p>
   * Removes old and inserts new creator authorizations.
   */
  @Override
  public void addCreatorAuth(Set<Long> apisIds, Set<Long> creatorIds) {
    apisAuthRepo.deleteByApisIdInAndCreator(apisIds, true);
    // Save api creator authorization
    List<ApisAuth> apisAuths = apisIds.stream()
        .flatMap(apisId -> creatorIds.stream()
            .map(creatorId -> toApisCreatorAuth(apisId, creatorId, uidGenerator)))
        .toList();
    batchInsert(apisAuths, "authObjectId");
  }

  /**
   * Batch add creator authorization for APIs with mapping.
   * <p>
   * Removes old and inserts new creator authorizations.
   */
  @Override
  public void addCreatorAuth(Map<Long, Set<Long>> apisIdAndCreatorIds) {
    apisAuthRepo.deleteByApisIdInAndCreator(apisIdAndCreatorIds.keySet(), true);
    // Save api creator authorization
    List<ApisAuth> apisAuths = apisIdAndCreatorIds.entrySet().stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(creatorId -> toApisCreatorAuth(entry.getKey(), creatorId, uidGenerator)))
        .toList();
    batchInsert(apisAuths, "authObjectId");
  }

  /**
   * Move creator authorization to a new project.
   * <p>
   * Removes old and inserts new creator authorizations for the target.
   */
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
