package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceExisted;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_FAVOURITE_REPEATED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseFavouriteCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavourite;
import cloud.xcan.angus.core.tester.domain.func.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class FuncCaseFavouriteCmdImpl extends CommCmd<FuncCaseFavourite, Long> implements
    FuncCaseFavouriteCmd {

  @Resource
  private FuncCaseFavouriteRepo funcCaseFavouriteRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncCaseFavourite favourite) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        // Check the case existed
        caseDb = funcCaseQuery.checkAndFindInfo(favourite.getCaseId());

        // Check the favourites is not repeated
        FuncCaseFavourite existed = funcCaseFavouriteRepo.findByCaseIdAndCreatedBy(
            favourite.getCaseId(), getUserId());
        assertResourceExisted(existed, CASE_FAVOURITE_REPEATED_T, new Object[]{caseDb.getName()});
      }

      @Override
      protected IdKey<Long, Object> process() {
        favourite.setProjectId(caseDb.getProjectId());
        IdKey<Long, Object> idKey = insert(favourite);

        //Add favorite case activity
        activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FAVOURITE));
        return idKey;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancel(Long caseId) {
    new BizTemplate<Void>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        // Check the case existed
        caseDb = funcCaseQuery.checkAndFindInfo(caseId);
      }

      @Override
      protected Void process() {
        if (funcCaseFavouriteRepo.deleteByCaseIdAndCreatedBy(caseId, getUserId()) > 0) {
          //Add cancel favorite case activity
          activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FAVOURITE_CANCEL));
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
      protected Void process() {
        if (isNull(projectId)) {
          funcCaseFavouriteRepo.deleteByCreatedBy(getUserId());
        } else {
          funcCaseFavouriteRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<FuncCaseFavourite, Long> getRepository() {
    return this.funcCaseFavouriteRepo;
  }
}




