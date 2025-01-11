package cloud.xcan.sdf.core.angustester.application.cmd.func.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_PLAN;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.query.common.impl.CommonQueryImpl.isAdmin;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.BACK;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncPlanCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncTrashCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncTrashQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanRepo;
import cloud.xcan.sdf.core.angustester.domain.func.plan.auth.FuncPlanAuthRepo;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrashRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class FuncTrashCmdImpl extends CommCmd<FuncTrash, Long> implements FuncTrashCmd {

  @Resource
  private FuncTrashRepo funcTrashRepo;

  @Resource
  private FuncTrashQuery funcTrashQuery;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private FuncCaseCmd funcCaseCmd;

  @Resource
  private FuncPlanRepo funcPlanRepo;

  @Resource
  private FuncPlanAuthRepo funcPlanAuthRepo;

  @Resource
  private FuncPlanCmd funcPlanCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Override
  public void add0(List<FuncTrash> trashes) {
    batchInsert0(trashes);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clear(Long id) {
    new BizTemplate<Void>() {
      FuncTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = funcTrashQuery.findMyTrashForBiz(id, "CLEAR");
      }

      @Override
      protected Void process() {
        // Delete trash
        funcTrashRepo.deleteById(id);

        // Delete association data
        deleteAssociation(singletonList(trashDb));

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void clearAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        // Find existed trash
        List<FuncTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isEmpty(allTrashes)) {
          return null;
        }

        // Delete all trash
        funcTrashRepo.deleteByTargetIdIn(allTrashes.stream().map(FuncTrash::getTargetId)
            .collect(Collectors.toList()));

        // Delete association data
        deleteAssociation(allTrashes);

        // No activity, Only record delete operation activities
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void back(Long id) {
    new BizTemplate<Void>() {
      FuncTrash trashDb;

      @Override
      protected void checkParams() {
        // Check the trash existed and permission
        trashDb = funcTrashQuery.findMyTrashForBiz(id, "BACK");
      }

      @Override
      protected Void process() {
        // Bach plan and case
        if (trashDb.getTargetType().isPlan()) {
          backPlan(trashDb.getTargetId(), true);
        } else if (trashDb.getTargetType().isCase()) {
          backCases(trashDb.getTargetId());
        }
        return null;
      }

      private void backCases(Long caseId) {
        // Cascade recovery current case
        List<Long> caseIds = List.of(caseId);
        funcCaseRepo.updateToUndeletedStatusByIdIn(caseIds);

        Optional<FuncCaseInfo> caseDb = funcCaseInfoRepo.find0ById(trashDb.getTargetId());
        caseDb.ifPresent(funcCaseInfo -> backPlan(funcCaseInfo.getPlanId(), false));

        // Delete case trash
        funcTrashRepo.deleteByTargetIdIn(caseIds);

        // Add back case activity
        activityCmd.add(toActivity(FUNC_CASE, trashDb, BACK, trashDb.getName()));
      }

      private void backPlan(Long planId, boolean saveActivity) {
        // Cascade recovery current plan
        List<Long> planIds = List.of(planId);
        funcPlanRepo.updateToUndeletedStatusByIdIn(planIds);

        // Delete associated plan trash
        funcTrashRepo.deleteByTargetIdIn(planIds);

        // Update case parent plan delete status
        funcCaseRepo.updatePlanDeleteStatusByPlan(planIds, false);

        // Add back plan activity
        if (saveActivity) {
          activityCmd.add(toActivity(FUNC_PLAN, trashDb, BACK, trashDb.getName()));
        }
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void backAll(Long projectId) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        List<FuncTrash> allTrashes = getAllTrashesByProject(projectId);

        if (isNotEmpty(allTrashes)) {
          List<Long> planIds = allTrashes.stream().filter(d -> d.getTargetType().isPlan())
              .map(FuncTrash::getTargetId).collect(Collectors.toList());
          if (isNotEmpty(planIds)) {
            backAllPlan(planIds);
          }

          List<Long> caseIds = allTrashes.stream().filter(d -> d.getTargetType().isCase())
              .map(FuncTrash::getTargetId).collect(Collectors.toList());
          if (isNotEmpty(caseIds)) {
            backAllCase(caseIds);
          }
        }
        // No activity
        return null;
      }

      private void backAllCase(List<Long> caseIds) {
        funcCaseRepo.updateToUndeletedStatusByIdIn(caseIds);

        // Delete case trash
        funcTrashRepo.deleteByTargetIdIn(caseIds);

        // Cascade recovery parent plans
        List<Long> planIds = funcCaseInfoRepo.findAll0PlanIdByIdIn(caseIds);
        if (isNotEmpty(planIds)) {
          backAllPlan(planIds);
        }
      }

      private void backAllPlan(List<Long> planIds) {
        // Update plan delete flag
        funcPlanRepo.updateToUndeletedStatusByIdIn(planIds);

        // Update case plan delete flag
        funcCaseRepo.updatePlanDeleteStatusByPlan(planIds, false);

        // Delete plan trash
        funcTrashRepo.deleteByTargetIdIn(planIds);
      }
    }.execute();
  }

  private void deleteAssociation(List<FuncTrash> trashes) {
    List<Long> allCaseIds = new ArrayList<>();

    List<Long> planIds = trashes.stream().filter(d -> d.getTargetType().isPlan())
        .map(FuncTrash::getTargetId).collect(Collectors.toList());
    if (ObjectUtils.isNotEmpty(planIds)) {
      List<Long> planCaseIds = funcCaseRepo.findAll0IdByPlanIdIn(planIds);
      if (isNotEmpty(planCaseIds)) {
        allCaseIds.addAll(planCaseIds);
      }

      funcPlanCmd.delete0(planIds);
      funcPlanAuthRepo.deleteByPlanIdIn(planIds);
      funcTrashRepo.deleteByTargetIdIn(planIds);
    }

    List<Long> caseIds = trashes.stream().filter(d -> d.getTargetType().isCase())
        .map(FuncTrash::getTargetId).collect(Collectors.toList());
    if (ObjectUtils.isNotEmpty(caseIds)) {
      allCaseIds.addAll(caseIds);
    }

    if (ObjectUtils.isNotEmpty(allCaseIds)) {
      funcCaseCmd.delete0(allCaseIds);
      funcTrashRepo.deleteByTargetIdIn(allCaseIds);
    }
  }

  private List<FuncTrash> getAllTrashesByProject(Long projectId) {
    Long currentUserId = getUserId();
    List<FuncTrash> trashDbs;
    if (isNull(projectId)) {
      trashDbs = isAdmin() ? funcTrashRepo.findAll() : funcTrashRepo.findByCreatedBy(currentUserId);
    } else {
      trashDbs = isAdmin() ? funcTrashRepo.findByProjectId(projectId)
          : funcTrashRepo.findByProjectIdAndCreatedBy(projectId, currentUserId);
    }
    return trashDbs;
  }

  @Override
  protected BaseRepository<FuncTrash, Long> getRepository() {
    return this.funcTrashRepo;
  }
}