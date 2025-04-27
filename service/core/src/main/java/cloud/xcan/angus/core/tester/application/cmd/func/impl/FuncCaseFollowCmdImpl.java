package cloud.xcan.angus.core.tester.application.cmd.func.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginMessage.CASE_FOLLOW_REPEATED_T;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.isNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseFollowCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollow;
import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class FuncCaseFollowCmdImpl extends CommCmd<FuncCaseFollow, Long> implements
    FuncCaseFollowCmd {

  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;

  @Resource
  private FuncCaseQuery funcCaseQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncCaseFollow follow) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        // Check the case existed
        caseDb = funcCaseQuery.checkAndFindInfo(follow.getCaseId());

        // Check the is not repeated
        if (funcCaseFollowRepo.countByCaseIdAndCreatedBy(follow.getCaseId(), getUserId()) > 0) {
          throw ResourceExisted.of(CASE_FOLLOW_REPEATED_T, new Object[]{caseDb.getName()});
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        follow.setProjectId(caseDb.getProjectId());
        IdKey<Long, Object> idKey = insert(follow);

        // Add follow case activity
        activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FOLLOW));
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
        if (funcCaseFollowRepo.deleteByCaseIdAndCreatedBy(caseId, getUserId()) > 0) {
          //Add cancel follow case activity
          activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FOLLOW_CANCEL));
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
          funcCaseFollowRepo.deleteByCreatedBy(getUserId());
        } else {
          funcCaseFollowRepo.deleteByProjectIdAndCreatedBy(projectId, getUserId());
        }
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<FuncCaseFollow, Long> getRepository() {
    return this.funcCaseFollowRepo;
  }
}




