package cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.test.plan.FuncPlanStatus;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.review.FuncReviewDetailVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class FuncReviewAssembler {

  public static FuncReview addDtoToDomain(FuncReviewAddDto dto) {
    return new FuncReview()
        .setName(dto.getName())
        .setPlanId(dto.getPlanId())
        .setStatus(FuncPlanStatus.PENDING)
        .setOwnerId(dto.getOwnerId())
        .setParticipantIds(dto.getParticipantIds())
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription())
        .setCaseIds(dto.getCaseIds());
  }

  public static FuncReview updateDtoToDomain(FuncReviewUpdateDto dto) {
    return new FuncReview()
        .setId(dto.getId())
        .setName(dto.getName())
        //.setPlanId(dto.getPlanId())
        //.setStatus(dto.getStatus())
        .setOwnerId(dto.getOwnerId())
        .setParticipantIds(dto.getParticipantIds())
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription());
  }

  public static FuncReview replaceDtoToDomain(FuncReviewReplaceDto dto) {
    return new FuncReview()
        .setId(dto.getId())
        .setName(dto.getName())
        .setPlanId(nonNull(dto.getId()) ? null : dto.getPlanId())
        .setStatus(nonNull(dto.getId()) ? null : FuncPlanStatus.PENDING)
        .setOwnerId(dto.getOwnerId())
        .setParticipantIds(dto.getParticipantIds())
        .setAttachments(dto.getAttachments())
        .setDescription(dto.getDescription());
  }

  public static FuncReviewDetailVo toDetailVo(FuncReview review) {
    return new FuncReviewDetailVo()
        .setId(review.getId())
        .setName(review.getName())
        .setProjectId(review.getProjectId())
        .setPlanId(review.getPlanId())
        .setStatus(review.getStatus())
        .setOwnerId(review.getOwnerId())
        .setOwnerName(review.getOwnerName())
        .setOwnerAvatar(review.getOwnerAvatar())
        .setParticipants(review.getParticipants())
        .setAttachments(review.getAttachments())
        .setDescription(review.getDescription())
        .setTenantId(review.getTenantId())
        .setCreatedBy(review.getCreatedBy())
        .setCreatedDate(review.getCreatedDate())
        .setModifiedBy(review.getModifiedBy())
        .setModifiedDate(review.getModifiedDate())
        .setCaseNum(review.getCaseNum())
        .setProgress(review.getProgress());
  }

  public static GenericSpecification<FuncReview> getSpecification(FuncReviewFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "ownerId", "createdBy", "createdDate", "modifiedBy")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

}
