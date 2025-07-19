package cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler;

import static java.util.Objects.isNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class FuncReviewCaseAssembler {

  public static FuncReviewCase reviewDtoToDomain(FuncReviewCaseDto dto) {
    return new FuncReviewCase().setId(dto.getId()).setReviewStatus(dto.getReviewStatus())
        .setReviewRemark(dto.getReviewRemark());
  }

  public static FuncReviewCaseDetailVo toDetail(FuncReviewCase reviewCase) {
    return new FuncReviewCaseDetailVo()
        .setId(reviewCase.getId())
        .setPlanId(reviewCase.getPlanId())
        //.setPlanName(reviewCase.getPlanName())
        .setReviewId(reviewCase.getReviewId())
        //.setReviewName(reviewCase.getReviewName())
        .setCaseId(reviewCase.getCaseId())
        .setReviewedCase(reviewCase.getReviewedCase())
        //.setLatestCase(reviewCase.getLatestCase())
        .setReviewerId(reviewCase.getReviewerId())
        //.setReviewerName(reviewCase.getReviewerName())
        .setReviewDate(reviewCase.getReviewDate())
        .setReviewStatus(reviewCase.getReviewStatus())
        .setReviewRemark(reviewCase.getReviewRemark())
        .setCreatedBy(reviewCase.getCreatedBy())
        //.setCreatedByName(reviewCase.getCreatedByName())
        .setCreatedDate(reviewCase.getCreatedDate());
  }

  public static FuncReviewCaseVo toListVo(FuncReviewCase reviewCase) {
    return new FuncReviewCaseVo()
        .setId(reviewCase.getId())
        .setPlanId(reviewCase.getPlanId())
        //.setPlanName(reviewCase.getPlanName())
        .setReviewId(reviewCase.getReviewId())
        //.setReviewName(reviewCase.getReviewName())
        .setCaseId(reviewCase.getCaseId())
        //.setReviewedCase(reviewCase.getReviewedCases())
        .setCaseInfo(isNull(reviewCase.getCaseInfo())
            ? null : FuncCaseAssembler.toListVo(reviewCase.getCaseInfo()))
        .setReviewerId(reviewCase.getReviewerId())
        //.setReviewerName(reviewCase.getReviewerName())
        .setReviewDate(reviewCase.getReviewDate())
        .setReviewStatus(reviewCase.getReviewStatus())
        .setReviewRemark(reviewCase.getReviewRemark())
        .setCreatedBy(reviewCase.getCreatedBy())
        //.setCreatedByName(reviewCase.getCreatedByName())
        .setCreatedDate(reviewCase.getCreatedDate());
  }

  public static GenericSpecification<FuncReviewCase> getSpecification(FuncReviewCaseFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("reviewDate")
        .inAndNotFields("id", "reviewId", "planId", "reviewerId")
        .orderByFields("id", "reviewId", "planId", "reviewerId", "reviewDate", "createdDate")
        .matchSearchFields("caseName", "caseCode")
        .build();
    return new GenericSpecification<>(filters);
  }

}
