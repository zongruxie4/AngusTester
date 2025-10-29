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
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollow;
import cloud.xcan.angus.core.tester.domain.test.follow.FuncCaseFollowRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional case follows.
 * </p>
 * <p>
 * Provides methods for adding and canceling case follows for functional testing.
 * Handles permission checks, case existence validation, and activity logging.
 * </p>
 * <p>
 * Key features include follow case management, user tracking preferences,
 * and comprehensive activity logging for audit purposes.
 * </p>
 */
@Biz
public class FuncCaseFollowCmdImpl extends CommCmd<FuncCaseFollow, Long> implements
    FuncCaseFollowCmd {

  @Resource
  private FuncCaseFollowRepo funcCaseFollowRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * Add a follow for a functional test case.
   * <p>
   * Checks if the case exists and prevents duplicate follows.
   * <p>
   * Logs follow activity.
   */
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

  /**
   * Cancel a follow for a functional test case.
   * <p>
   * Checks if the case exists before canceling.
   * <p>
   * Logs cancel follow activity if deletion is successful.
   */
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

  /**
   * Cancel all follows for the current user, optionally by project.
   * <p>
   * If projectId is null, cancels all follows for the user across all projects.
   * <p>
   * Otherwise, cancels follows only within the specified project.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void cancelAll(Long projectId) {
    new BizTemplate<Void>() {

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

  /**
   * Get the repository for functional case follows.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncCaseFollow, Long> getRepository() {
    return this.funcCaseFollowRepo;
  }
}




