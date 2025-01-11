package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler;

import cloud.xcan.sdf.api.angustester.indicator.vo.PerfVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Objects;
import java.util.Set;

public class IndicatorPerfAssembler {

  public static IndicatorPerf addDtoToDomain(PerfAddDto dto, CombinedTargetType targetType) {
    return new IndicatorPerf()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setThreads(dto.getThreads())
        .setDuration(dto.getDuration())
        .setRampUpThreads(dto.getRampUpThreads())
        .setRampUpInterval(dto.getRampUpInterval())
        .setArt(dto.getArt())
        .setPercentile(dto.getPercentile())
        .setTps(dto.getTps())
        .setErrorRate(dto.getErrorRate());
  }

  public static IndicatorPerf replaceDtoToDomain(PerfReplaceDto dto,
      CombinedTargetType targetType) {
    return new IndicatorPerf()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setThreads(dto.getThreads())
        .setDuration(dto.getDuration())
        .setRampUpThreads(dto.getRampUpThreads())
        .setRampUpInterval(dto.getRampUpInterval())
        .setArt(dto.getArt())
        .setPercentile(dto.getPercentile())
        .setTps(dto.getTps())
        .setErrorRate(dto.getErrorRate());
  }

  public static PerfVo toIndicatorPerfVo(IndicatorPerf perf) {
    if (Objects.isNull(perf)) {
      return null;
    }
    return new PerfVo().setId(perf.getId())
        .setTargetType(perf.getTargetType())
        .setTargetId(perf.getTargetId())
        .setTargetName(perf.getTargetName())
        .setThreads(perf.getThreads())
        .setDuration(perf.getDuration())
        .setRampUpThreads(perf.getRampUpThreads())
        .setRampUpInterval(perf.getRampUpInterval())
        .setArt(perf.getArt())
        .setPercentile(perf.getPercentile())
        .setTps(perf.getTps())
        .setErrorRate(perf.getErrorRate());
  }

  public static PerfListVo toApisPerfListVo(IndicatorPerf perf) {
    return new PerfListVo().setId(perf.getId())
        .setTargetType(perf.getTargetType())
        .setTargetId(perf.getTargetId())
        .setTargetName(perf.getTargetName())
        .setTargetType(perf.getTargetType())
        .setThreads(perf.getThreads())
        .setDuration(perf.getDuration())
        .setRampUpThreads(perf.getRampUpThreads())
        .setRampUpInterval(perf.getRampUpInterval())
        .setArt(perf.getArt())
        .setPercentile(perf.getPercentile())
        .setTps(perf.getTps())
        .setErrorRate(perf.getErrorRate())
        .setCreatedBy(perf.getCreatedBy())
        .setCreatedDate(perf.getCreatedDate());
  }

  public static GenericSpecification<IndicatorPerf> getSpecification(PerfFindDto dto) {
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

  public static Set<SearchCriteria> getSearchCriteria(PerfSearchDto dto) {
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
