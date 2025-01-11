package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler;

import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaseline;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Set;

public class FuncBaselineAssembler {

  public static FuncBaseline toAddDomain(FuncBaselineAddDto dto) {
    return new FuncBaseline().setName(dto.getName())
        .setPlanId(dto.getPlanId())
        .setDescription(dto.getDescription())
        .setEstablishedFlag(false)
        .setCaseIds(dto.getCaseIds());
  }

  public static FuncBaseline toUpdateDomain(FuncBaselineUpdateDto dto) {
    return new FuncBaseline().setId(dto.getId())
        .setName(dto.getName()).setDescription(dto.getDescription());
  }

  public static FuncBaseline toReplaceDomain(FuncBaselineReplaceDto dto) {
    return new FuncBaseline().setId(dto.getId())
        .setName(dto.getName())
        .setPlanId(isNull(dto.getId()) ? dto.getPlanId() : null)
        .setDescription(dto.getDescription())
        .setCaseIds(isNull(dto.getId()) ? dto.getCaseIds() : null);
  }

  public static FuncBaselineDetailVo toDetailVo(FuncBaseline baseline) {
    return new FuncBaselineDetailVo().setId(baseline.getId())
        .setName(baseline.getName())
        .setPlanId(baseline.getPlanId())
        .setDescription(baseline.getDescription())
        .setEstablishedFlag(baseline.getEstablishedFlag())
        .setCaseIds(baseline.getCaseIds())
        .setCreatedBy(baseline.getCreatedBy())
        .setCreatedDate(baseline.getCreatedDate())
        .setLastModifiedBy(baseline.getLastModifiedBy())
        .setLastModifiedDate(baseline.getLastModifiedDate());
  }

  public static FuncBaselineVo toListVo(FuncBaselineInfo baseline) {
    return new FuncBaselineVo().setId(baseline.getId())
        .setName(baseline.getName())
        .setPlanId(baseline.getPlanId())
        .setDescription(baseline.getDescription())
        .setEstablishedFlag(baseline.getEstablishedFlag())
        .setCreatedBy(baseline.getCreatedBy())
        .setCreatedByName(baseline.getCreatedByName())
        .setCreatedByAvatar(baseline.getCreatedByAvatar())
        .setCreatedDate(baseline.getCreatedDate())
        .setLastModifiedBy(baseline.getLastModifiedBy())
        .setLastModifiedDate(baseline.getLastModifiedDate());
  }

  public static GenericSpecification<FuncBaselineInfo> getSpecification(FuncBaselineFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy")
        .matchSearchFields("name", "description")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(FuncBaselineSearchDto dto) {
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "name", "createdBy", "createdDate", "lastModifiedBy")
        .matchSearchFields("name", "description")
        .build();
  }

}
