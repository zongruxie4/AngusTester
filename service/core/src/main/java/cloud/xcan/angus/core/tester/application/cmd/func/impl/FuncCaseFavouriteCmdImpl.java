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
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavourite;
import cloud.xcan.angus.core.tester.domain.test.favourite.FuncCaseFavouriteRepo;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Command implementation for managing functional case favorites.
 * </p>
 * <p>
 * Provides methods for adding and canceling favorite cases for functional testing.
 * Handles permission checks, case existence validation, and activity logging.
 * </p>
 * <p>
 * Key features include favorite case management, user preference tracking,
 * and comprehensive activity logging for audit purposes.
 * </p>
 */
@Biz
public class FuncCaseFavouriteCmdImpl extends CommCmd<FuncCaseFavourite, Long> implements
    FuncCaseFavouriteCmd {

  @Resource
  private FuncCaseFavouriteRepo funcCaseFavouriteRepo;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private ActivityCmd activityCmd;

  /**
   * <p>
   * Add a functional case to favorites.
   * </p>
   * <p>
   * Checks case existence and prevents duplicate favorites. Adds the case to user's favorites
   * and logs the activity. Validates that the case exists and is not already favourited.
   * </p>
   * @param favourite the favorite case entity to add
   * @return ID and name of the created favorite
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add(FuncCaseFavourite favourite) {
    return new BizTemplate<IdKey<Long, Object>>() {
      FuncCaseInfo caseDb;

      @Override
      protected void checkParams() {
        // Validate case exists and retrieve details
        caseDb = funcCaseQuery.checkAndFindInfo(favourite.getCaseId());

        // Check if favorite already exists to prevent duplicates
        FuncCaseFavourite existed = funcCaseFavouriteRepo.findByCaseIdAndCreatedBy(
            favourite.getCaseId(), getUserId());
        assertResourceExisted(existed, CASE_FAVOURITE_REPEATED_T, new Object[]{caseDb.getName()});
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Set project ID and insert favorite case record
        favourite.setProjectId(caseDb.getProjectId());
        IdKey<Long, Object> idKey = insert(favourite);

        // Log favorite case activity
        activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FAVOURITE));
        return idKey;
      }
    }.execute();
  }

  /**
   * Cancel a favorite for a functional test case.
   * <p>
   * Checks if the case exists before canceling.
   * <p>
   * Logs cancel favorite activity if deletion is successful.
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
        if (funcCaseFavouriteRepo.deleteByCaseIdAndCreatedBy(caseId, getUserId()) > 0) {
          //Add cancel favorite case activity
          activityCmd.add(toActivity(FUNC_CASE, caseDb, ActivityType.FAVOURITE_CANCEL));
        }
        return null;
      }
    }.execute();
  }

  /**
   * Cancel all favorites for the current user, optionally by project.
   * <p>
   * If projectId is null, cancels all favorites for the user across all projects.
   * <p>
   * Otherwise, cancels favorites only within the specified project.
   */
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

  /**
   * Get the repository for functional case favorites.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<FuncCaseFavourite, Long> getRepository() {
    return this.funcCaseFavouriteRepo;
  }
}




