package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler;

import cloud.xcan.sdf.api.angustester.indicator.vo.FuncVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Objects;
import java.util.Set;

public class IndicatorFuncAssembler {

  public static IndicatorFunc addDtoToDomain(FuncAddDto dto, CombinedTargetType targetType) {
    return new IndicatorFunc()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setSmoke(dto.isSmoke())
        .setSmokeCheckSetting(dto.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(dto.getUserDefinedSmokeAssertion())
        .setSecurity(dto.isSecurity())
        .setSecurityCheckSetting(dto.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(dto.getUserDefinedSecurityAssertion());
  }

  public static IndicatorFunc replaceDtoToDomain(FuncReplaceDto dto,
      CombinedTargetType targetType) {
    return new IndicatorFunc()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setSmoke(dto.isSmoke())
        .setSmokeCheckSetting(dto.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(dto.getUserDefinedSmokeAssertion())
        .setSecurity(dto.isSecurity())
        .setSecurityCheckSetting(dto.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(dto.getUserDefinedSecurityAssertion());
  }

  public static FuncVo toIndicatorFuncVo(IndicatorFunc perf) {
    if (Objects.isNull(perf)) {
      return null;
    }
    return new FuncVo().setId(perf.getId())
        .setTargetType(perf.getTargetType())
        .setTargetId(perf.getTargetId())
        .setTargetName(perf.getTargetName())
        .setSmoke(perf.isSmoke())
        .setSmokeCheckSetting(perf.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(perf.getUserDefinedSmokeAssertion())
        .setSecurity(perf.isSecurity())
        .setSecurityCheckSetting(perf.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(perf.getUserDefinedSecurityAssertion());
  }

  public static FuncListVo toApisFuncListVo(IndicatorFunc perf) {
    return new FuncListVo().setId(perf.getId())
        .setTargetType(perf.getTargetType())
        .setTargetId(perf.getTargetId())
        .setTargetName(perf.getTargetName())
        .setTargetType(perf.getTargetType())
        .setSmoke(perf.isSmoke())
        .setSmokeCheckSetting(perf.getSmokeCheckSetting())
        .setUserDefinedSmokeAssertion(perf.getUserDefinedSmokeAssertion())
        .setSecurity(perf.isSecurity())
        .setSecurityCheckSetting(perf.getSecurityCheckSetting())
        .setUserDefinedSecurityAssertion(perf.getUserDefinedSecurityAssertion())
        .setCreatedBy(perf.getCreatedBy())
        .setCreatedDate(perf.getCreatedDate());
  }

  public static GenericSpecification<IndicatorFunc> getSpecification(FuncFindDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
    }
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "endpoint")
        .rangeSearchFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(FuncSearchDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
    }
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("targetName")
        .rangeSearchFields("id", "createdDate")
        .inAndNotFields("id", "targetType")
        .build();
  }
}
