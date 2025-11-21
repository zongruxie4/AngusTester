package cloud.xcan.angus.core.tester.interfaces.config.facade.internal.assembler;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncAddDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncFindDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.dto.FuncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.FuncListVo;
import cloud.xcan.angus.core.tester.interfaces.config.facade.vo.FuncVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
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
    if (Objects.nonNull(dto.getAdmin()) && dto.getAdmin()) {
      dto.getFilters().add(SearchCriteria.equal("admin", "true"));
    }
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("name", "endpoint")
        .rangeSearchFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

}
