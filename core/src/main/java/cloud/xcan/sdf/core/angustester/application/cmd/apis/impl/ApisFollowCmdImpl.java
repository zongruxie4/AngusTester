package cloud.xcan.sdf.core.angustester.application.cmd.apis.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.APIS_FOLLOW_REPEATED_T;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisFollowCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollow;
import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollowRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

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
          apisFollowRepo.deleteByCreatedBy(getUserId());
        } else {
          apisFollowRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ApisFollow, Long> getRepository() {
    return this.apisFollowRepo;
  }
}
