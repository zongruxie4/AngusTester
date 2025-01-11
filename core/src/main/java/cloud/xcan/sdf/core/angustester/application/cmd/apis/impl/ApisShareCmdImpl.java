package cloud.xcan.sdf.core.angustester.application.cmd.apis.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SHARE_CREATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SHARE_DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SHARE_UPDATED;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PUBLIC_TOKEN_LENGTH;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisShareCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisShareQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisBaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareRepo;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ApisShareCmdImpl extends CommCmd<ApisShare, Long> implements ApisShareCmd {

  @Resource
  private ApisShareRepo apisShareRepo;

  @Resource
  private ApisShareQuery apisShareQuery;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisAuthQuery apisAuthQuery;

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public ApisShare add(ApisShare share) {
    return new BizTemplate<ApisShare>() {
      Services servicesDb;
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check services exists
        servicesDb = servicesQuery.checkAndFind(share.getServicesId());
        // Check apis not empty
        if (!share.getShareScope().isService()) {
          assertNotEmpty(share.getApisIds(), "Share apis id must not be empty");
        }
        // Check share permission and exists
        if (share.getShareScope().isSingleApi()) {
          long apisId = share.getApisIds().iterator().next();
          apisAuthQuery.checkShareAuth(getUserId(), apisId);
          apisDb = apisQuery.checkAndFindBaseInfo(apisId);
        } else {
          servicesAuthQuery.checkShareAuth(getUserId(), share.getServicesId());
        }
      }

      @Override
      protected ApisShare process() {
        share.setProjectId(servicesDb.getProjectId());
        share.setPat(randomAlphanumeric(MAX_PUBLIC_TOKEN_LENGTH));

        insert0(share);

        // Save activity
        if (share.getShareScope().isSingleApi()) {
          activityCmd.add(toActivity(API, apisDb, SHARE_CREATED, share.getId()));
        } else {
          activityCmd.add(toActivity(SERVICE, servicesDb, SHARE_CREATED, share.getId()));
        }
        return share;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(ApisShare share) {
    new BizTemplate<Void>() {
      ApisShare shareDb;

      @Override
      protected void checkParams() {
        // Check the share exists
        shareDb = apisShareQuery.checkAndFind(share.getId());
      }

      @Override
      protected Void process() {
        apisShareRepo.save(CoreUtils.copyPropertiesIgnoreNull(share, shareDb));

        // Check share permission
        checkSharePermission(shareDb);

        // Save activity
        saveShareActivity(shareDb, SHARE_UPDATED);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<ApisShare> sharesDb;

      @Override
      protected void checkParams() {
        // Check and find
        sharesDb = apisShareQuery.checkAndFind(ids);
      }

      @Override
      protected Void process() {
        apisShareRepo.deleteByIdIn(ids);

        for (ApisShare shareDb : sharesDb) {
          // Check share permission
          checkSharePermission(shareDb);

          // Save activity
          saveShareActivity(shareDb, SHARE_DELETED);
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void incrView(Long id) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        apisShareRepo.incrViewById(id);
        return null;
      }
    }.execute();
  }

  private void checkSharePermission(ApisShare shareDb) {
    if (shareDb.getShareScope().isSingleApi()) {
      apisAuthQuery.checkShareAuth(getUserId(), shareDb.getApisIds().iterator().next());
    } else {
      servicesAuthQuery.checkShareAuth(getUserId(), shareDb.getServicesId());
    }
  }

  private void saveShareActivity(ApisShare shareDb, ActivityType type) {
    if (shareDb.getShareScope().isSingleApi()) {
      ApisBaseInfo apisDb = apisQuery.checkAndFindBaseInfo(
          shareDb.getApisIds().iterator().next());
      activityCmd.add(toActivity(API, apisDb, type, shareDb.getId()));
    } else {
      Services servicesDb = servicesQuery.checkAndFind(shareDb.getServicesId());
      activityCmd.add(toActivity(SERVICE, servicesDb, type, shareDb.getId()));
    }
  }

  @Override
  protected BaseRepository<ApisShare, Long> getRepository() {
    return apisShareRepo;
  }
}
