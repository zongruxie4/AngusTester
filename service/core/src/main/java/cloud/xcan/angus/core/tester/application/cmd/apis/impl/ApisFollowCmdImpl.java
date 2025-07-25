package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.APIS_FOLLOW_REPEATED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisFollowCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollow;
import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for managing API follows.
 * <p>
 * Provides methods for adding, canceling, and batch canceling API follows.
 * Ensures permission checks, duplicate prevention, and activity logging.
 */
@Biz
public class ApisFollowCmdImpl extends CommCmd<ApisFollow, Long> implements ApisFollowCmd {

  @Resource
  private ApisFollowRepo apisFollowRepo;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add an API to follows.
   * <p>
   * Validates API existence, permission, and duplicate, inserts follow, and logs the activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(ApisFollow follow) {
    return new BizTemplate<IdKey<Long, Object>>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis existed
        apisDb = apisQuery.checkAndFindBaseInfo(follow.getApisId());

        // Check the permission to view apis
        apisAuthQuery.checkViewAuth(getUserId(), follow.getApisId());

        // Check the not repeated
        if (apisFollowRepo.countByApisIdAndCreatedBy(follow.getApisId(), getUserId()) > 0) {
          throw ResourceExisted.of(APIS_FOLLOW_REPEATED_T, new Object[]{apisDb.getName()});
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        follow.setProjectId(apisDb.getProjectId());
        IdKey<Long, Object> idKey = insert(follow);

        // Add follow api activity
        activityCmd.add(toActivity(API, apisDb, ActivityType.FOLLOW));
        return idKey;
      }
    }.execute();
  }

  /**
   * Cancel an API follow.
   * <p>
   * Validates API existence, deletes follow, and logs the activity.
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
        if (apisFollowRepo.deleteByApisIdAndCreatedBy(apisId, getUserId()) > 0) {
          //Add cancel follow api activity
          activityCmd.add(toActivity(API, apisDb, ActivityType.FOLLOW_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Cancel all API follows for a project or user.
   * <p>
   * Deletes all follows for the given project or user.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        if (isNull(projectId)) {
          apisFollowRepo.deleteByCreatedBy(getUserId());
        } else {
          apisFollowRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  /**
   * Get the repository for ApisFollow entity.
   * <p>
   * @return the ApisFollowRepo instance
   */
  @Override
  protected BaseRepository<ApisFollow, Long> getRepository() {
    return this.apisFollowRepo;
  }
}
