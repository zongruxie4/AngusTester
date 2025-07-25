package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_FAVOURITE_REPEATED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavourite;
import cloud.xcan.angus.core.tester.domain.apis.favourite.ApisFavouriteRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for managing API favorites.
 * <p>
 * Provides methods for adding, canceling, and batch canceling API favorites.
 * Ensures permission checks, duplicate prevention, and activity logging.
 */
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

  /**
   * Add an API to favorites.
   * <p>
   * Validates API existence, permission, and duplicate, inserts favorite, and logs the activity.
   */
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

  /**
   * Cancel an API favorite.
   * <p>
   * Validates API existence, deletes favorite, and logs the activity.
   */
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

  /**
   * Cancel all API favorites for a project or user.
   * <p>
   * Deletes all favorites for the given project or user.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {

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

  /**
   * Get the repository for ApisFavourite entity.
   * <p>
   * @return the ApisFavouriteRepo instance
   */
  @Override
  protected BaseRepository<ApisFavourite, Long> getRepository() {
    return this.apisFavouriteRepo;
  }
}




