package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getCachedUidGenerator;

import cloud.xcan.angus.core.tester.domain.func.FuncTargetType;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlan;
import cloud.xcan.angus.core.tester.domain.func.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.func.summary.FuncPlanSummary;
import cloud.xcan.angus.core.tester.domain.func.trash.FuncTrash;
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
    plan.setAuth(planDb.getAuth())
        .setStatus(planDb.getStatus())
        .setProjectId(planDb.getProjectId())
        .setDeleted(planDb.getDeleted())
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
        .setAuth(planDb.getAuth())
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
        .setReview(planDb.getReview())
        .setEvalWorkloadMethod(planDb.getEvalWorkloadMethod())
        .setOtherInformation(planDb.getOtherInformation())
        .setDeleted(false);
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
        .setReview(plan.getReview())
        .setEvalWorkloadMethod(plan.getEvalWorkloadMethod())
        .setAuth(plan.getAuth())
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
