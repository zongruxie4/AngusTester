package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.core.spring.SpringContextHolder.getCachedUidGenerator;

import cloud.xcan.sdf.core.angustester.domain.func.FuncTargetType;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlan;
import cloud.xcan.sdf.core.angustester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.sdf.core.angustester.domain.func.summary.FuncPlanSummary;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import java.time.LocalDateTime;

public class FuncPlanConverter {

  public static FuncTrash toFuncTrash(FuncPlan planDb) {
    return new FuncTrash()
        .setId(getCachedUidGenerator().getUID())
        .setProjectId(planDb.getProjectId())
        .setPlanId(planDb.getId())
        .setTargetType(FuncTargetType.PLAN)
        .setTargetId(planDb.getId())
        .setTargetName(planDb.getName())
        .setCreatedBy(planDb.getCreatedBy())
        .setDeletedBy(getUserId())
        .setDeletedDate(LocalDateTime.now());
  }

  public static void setReplaceInfo(FuncPlan plan, FuncPlan planDb) {
    plan.setAuthFlag(planDb.getAuthFlag())
        .setStatus(planDb.getStatus())
        .setProjectId(planDb.getProjectId())
        .setDeletedFlag(planDb.getDeletedFlag())
        .setDeletedBy(planDb.getDeletedBy())
        .setDeletedDate(planDb.getDeletedDate())
        .setTenantId(planDb.getTenantId())
        .setCreatedBy(planDb.getCreatedBy())
        .setCreatedDate(planDb.getCreatedDate());
  }

  public static FuncPlan clone(FuncPlan planDb) {
    return new FuncPlan()
        .setProjectId(planDb.getProjectId())
        .setName(planDb.getName())
        .setAuthFlag(planDb.getAuthFlag())
        .setStatus(FuncPlanStatus.PENDING)
        .setStartDate(planDb.getStartDate())
        .setDeadlineDate(planDb.getDeadlineDate())
        .setOwnerId(planDb.getOwnerId())
        .setTesterResponsibilities(planDb.getTesterResponsibilities())
        .setTestingScope(planDb.getTestingScope())
        .setTestingObjectives(planDb.getTestingObjectives())
        .setAcceptanceCriteria(planDb.getAcceptanceCriteria())
        .setOtherInformation(planDb.getOtherInformation())
        .setCasePrefix(planDb.getCasePrefix())
        .setReviewFlag(planDb.getReviewFlag())
        .setEvalWorkloadMethod(planDb.getEvalWorkloadMethod())
        .setOtherInformation(planDb.getOtherInformation())
        .setDeletedFlag(false);
  }

  public static FuncPlanSummary toFuncPlanSummary(FuncPlan plan) {
    return new FuncPlanSummary()
        .setId(plan.getId())
        .setProjectId(plan.getProjectId())
        .setName(plan.getName())
        .setStatus(plan.getStatus())
        .setStartDate(plan.getStartDate())
        .setDeadlineDate(plan.getDeadlineDate())
        .setOwnerId(plan.getOwnerId())
        .setOwnerName(plan.getOwnerName())
        .setOwnerAvatar(plan.getOwnerAvatar())
        .setTesterResponsibilities(plan.getTesterResponsibilities())
        .setMembers(plan.getMembers())
        .setTestingScope(plan.getTestingScope())
        .setTestingObjectives(plan.getTestingObjectives())
        .setAcceptanceCriteria(plan.getAcceptanceCriteria())
        .setOtherInformation(plan.getOtherInformation())
        .setAttachments(plan.getAttachments())
        .setCasePrefix(plan.getCasePrefix())
        .setReviewFlag(plan.getReviewFlag())
        .setEvalWorkloadMethod(plan.getEvalWorkloadMethod())
        .setAuthFlag(plan.getAuthFlag())
        //.setTenantId(plan.getTenantId())
        .setCreatedBy(plan.getCreatedBy())
        .setCreatedDate(plan.getCreatedDate())
        .setLastModifiedBy(plan.getLastModifiedBy())
        .setLastModifiedDate(plan.getLastModifiedDate())
        .setCaseNum(plan.getCaseNum())
        .setValidCaseNum(plan.getValidCaseNum())
        .setProgress(plan.getProgress());
  }
}
