package cloud.xcan.sdf.core.angustester.application.cmd.apis.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_FAVOURITE_REPEATED_T;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisFavouriteCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.sdf.core.angustester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ApisFavouriteCmdImpl extends CommCmd<ApisFavourite, Long> implements ApisFavouriteCmd {

  @Resource
  private ApisFavouriteRepo apisFavouriteRepo;

  @Resource
  private ApisAuthQuery apisAuthQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisFavourite favourite) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis existed
        apisDb = apisQuery.checkAndFindBaseInfo(favourite.getApisId());

        // Check the permission to view apis
        apisAuthQuery.checkViewAuth(getUserId(), favourite.getApisId());

        // Check the favourites is not repeated
        ApisFavourite existed = apisFavouriteRepo.findByApisIdAndCreatedBy(favourite.getApisId(),
            getUserId());
        assertResourceExisted(existed, APIS_FAVOURITE_REPEATED_T, new Object[]{apisDb.getName()});
      }

      @Override
      protected IdKey<Long, Object> process() {
        favourite.setProjectId(apisDb.getProjectId());
        IdKey<Long, Object> idKey = insert(favourite);

        // Add favorite api activity
        activityCmd.add(toActivity(API, apisDb, ActivityType.FAVOURITE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancel(Long apisId) {
    new BizTemplate<Void>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis existed
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);
      }

      @Override
      protected Void process() {
        if (apisFavouriteRepo.deleteByApisIdAndCreatedBy(apisId, getUserId()) > 0) {
          //Add cancel favorite api activity
          activityCmd.add(toActivity(API, apisDb, ActivityType.FAVOURITE_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        if (isNull(projectId)) {
          apisFavouriteRepo.deleteByCreatedBy(getUserId());
        } else {
          apisFavouriteRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ApisFavourite, Long> getRepository() {
    return this.apisFavouriteRepo;
  }
}




