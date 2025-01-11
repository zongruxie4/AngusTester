package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class FuncBaselineCaseConverter {

  public static List<FuncBaselineCase> toBaselineCases(Long baselineId, List<FuncCase> cases) {
    return cases.stream().map(case0 -> {
      FuncBaselineCase baselineCase = new FuncBaselineCase();
      baselineCase.setBaselineId(baselineId)
          .setCaseId(case0.getId())
          .setName(case0.getName())
          .setCode(case0.getCode())
          .setVersion(case0.getVersion())
          .setProjectId(case0.getProjectId())
          .setPlanId(case0.getPlanId())
          .setModuleId(case0.getModuleId())
          .setPriority(case0.getPriority())
          .setDeadlineDate(case0.getDeadlineDate())
          .setOverdueFlag(case0.getOverdueFlag())
          .setEvalWorkloadMethod(case0.getEvalWorkloadMethod())
          .setEvalWorkload(case0.getEvalWorkload())
          .setActualWorkload(case0.getActualWorkload())
          .setPrecondition(case0.getPrecondition())
          .setSteps(case0.getSteps())
          .setDescription(case0.getDescription())
          .setReviewFlag(case0.getReviewFlag())
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
          .setCreatedBy(getUserId())
          .setCreatedDate(LocalDateTime.now());
      return baselineCase;
    }).collect(Collectors.toList());
  }

}
