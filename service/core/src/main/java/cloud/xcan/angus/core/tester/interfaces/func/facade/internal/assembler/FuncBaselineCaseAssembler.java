package cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.emptyList;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.FuncCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import java.util.Collections;
import java.util.Set;

public class FuncBaselineCaseAssembler {

  public static FuncBaselineCaseDetailVo toDetailVo(FuncBaselineCase case0) {
    return isNull(case0) ? null: new FuncBaselineCaseDetailVo()
        .setBaselineId(case0.getBaselineId())
        .setId(case0.getCaseId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setVersion(case0.getVersion())
        .setProjectId(case0.getProjectId())
        .setPlanId(case0.getPlanId())
        .setModuleId(case0.getModuleId())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setSteps(case0.getSteps())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTesterId(case0.getTesterId())
        .setDeveloperId(case0.getDeveloperId())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        .setAttachments(case0.getAttachments())
        .setTags(isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
            .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .toList() : Collections.emptyList())
        .setRefTaskInfos(isNotEmpty(case0.getAssocTasks()) ? case0.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).toList() : emptyList())
        .setRefCaseInfos(isNotEmpty(case0.getAssocCases()) ? case0.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).toList() : emptyList())
        .setFavourite(case0.getFavourite())
        .setFollow(case0.getFollow())
        .setCommentNum(case0.getCommentNum())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static FuncBaselineCaseListVo toListVo(FuncBaselineCaseInfo case0) {
    return new FuncBaselineCaseListVo()
        .setBaselineId(case0.getBaselineId())
        .setId(case0.getCaseId())
        .setName(case0.getName())
        .setCode(case0.getCode())
        .setVersion(case0.getVersion())
        .setPlanId(case0.getPlanId())
        .setModuleId(case0.getModuleId())
        .setPriority(case0.getPriority())
        .setDeadlineDate(case0.getDeadlineDate())
        .setOverdue(case0.getOverdue())
        .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
        .setEvalWorkload(case0.getEvalWorkload())
        .setActualWorkload(case0.getActualWorkload())
        .setPrecondition(case0.getPrecondition())
        .setSteps(case0.getSteps())
        .setRefTaskInfos(isNotEmpty(case0.getAssocTasks()) ? case0.getAssocTasks().stream()
            .map(TaskAssembler::toInfoVo).toList() : emptyList())
        .setRefCaseInfos(isNotEmpty(case0.getAssocCases()) ? case0.getAssocCases().stream()
            .map(FuncCaseAssembler::toInfoVo).toList() : emptyList())
        .setDescription(case0.getDescription())
        .setReview(case0.getReview())
        .setReviewerId(case0.getReviewerId())
        .setReviewDate(case0.getReviewDate())
        .setReviewStatus(case0.getReviewStatus())
        .setReviewRemark(case0.getReviewRemark())
        .setReviewNum(case0.getReviewNum())
        .setTesterId(case0.getTesterId())
        .setTesterName(case0.getTesterName())
        .setTesterAvatar(case0.getTesterAvatar())
        .setDeveloperId(case0.getDeveloperId())
        .setTestNum(case0.getTestNum())
        .setTestFailNum(case0.getTestFailNum())
        .setReviewFailNum(case0.getReviewFailNum())
        .setTestResult(case0.getTestResult())
        .setTestRemark(case0.getTestRemark())
        .setTestResultHandleDate(case0.getTestResultHandleDate())
        .setTags(isNotEmpty(case0.getTagTargets()) ? case0.getTagTargets().stream()
            .map(o -> new IdAndNameVo().setId(o.getTagId()).setName(o.getTagName()))
            .toList() : Collections.emptyList())
        .setFavourite(case0.getFavourite())
        .setFollow(case0.getFollow())
        .setTenantId(case0.getTenantId())
        .setCreatedBy(case0.getCreatedBy())
        .setCreatedByName(case0.getCreatedByName())
        .setCreatedDate(case0.getCreatedDate())
        .setAvatar(case0.getAvatar())
        .setLastModifiedBy(case0.getLastModifiedBy())
        .setLastModifiedDate(case0.getLastModifiedDate());
  }

  public static GenericSpecification<FuncBaselineCaseInfo> getSpecification(FuncCaseFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("deadlineDate", "createdDate", "lastModifiedDate", "reviewDate",
            "testResultHandleDate", "reviewNum", "testNum", "testFailNum")
        .inAndNotFields("id", "tagId", "testResult", "testerId", "developerId")
        .orderByFields("id", "createdDate", "lastModifiedDate", "priority",
            "deadlineDate", "reviewStatus", "reviewNum", "testerId", "developerId", "reviewerId",
            "testNum", "testFailNum", "testResult", "reviewDate", "testResultHandleDate")
        .matchSearchFields("name", "description", "code")
        .build();
    return new GenericSpecification<FuncBaselineCaseInfo>(filters);
  }

}
