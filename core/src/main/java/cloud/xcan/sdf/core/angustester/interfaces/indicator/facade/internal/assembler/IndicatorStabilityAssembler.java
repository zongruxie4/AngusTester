package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler;

import cloud.xcan.sdf.api.angustester.indicator.vo.StabilityVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder;
import java.util.Objects;
import java.util.Set;

public class IndicatorStabilityAssembler {

  public static IndicatorStability addDtoToDomain(StabilityAddDto dto,
      CombinedTargetType targetType) {
    return new IndicatorStability()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setThreads(dto.getThreads())
        .setDuration(dto.getDuration())
        .setArt(dto.getArt())
        .setPercentile(dto.getPercentile())
        .setTps(dto.getTps())
        .setErrorRate(dto.getErrorRate())
        .setCpu(dto.getCpu())
        .setDisk(dto.getDisk())
        .setMemory(dto.getMemory())
        .setNetwork(dto.getNetwork());
  }

  public static IndicatorStability replaceDtoToDomain(StabilityReplaceDto dto,
      CombinedTargetType targetType) {
    return new IndicatorStability()
        .setTargetId(dto.getTargetId())
        .setTargetType(targetType)
        .setThreads(dto.getThreads())
        .setDuration(dto.getDuration())
        .setArt(dto.getArt())
        .setPercentile(dto.getPercentile())
        .setTps(dto.getTps())
        .setErrorRate(dto.getErrorRate())
        .setCpu(dto.getCpu())
        .setMemory(dto.getMemory())
        .setDisk(dto.getDisk())
        .setNetwork(dto.getNetwork());
  }

  public static StabilityListVo toListVo(IndicatorStability stability) {
    if (Objects.isNull(stability)) {
      return null;
    }
    return new StabilityListVo().setId(stability.getId())
        .setTargetType(stability.getTargetType())
        .setTargetId(stability.getTargetId())
        .setTargetName(stability.getTargetName())
        .setThreads(stability.getThreads())
        .setDuration(stability.getDuration())
        .setArt(stability.getArt())
        .setPercentile(stability.getPercentile())
        .setTps(stability.getTps())
        .setErrorRate(stability.getErrorRate())
        .setCpu(stability.getCpu())
        .setMemory(stability.getMemory())
        .setDisk(stability.getDisk())
        .setNetwork(stability.getNetwork())
        .setCreatedBy(stability.getCreatedBy())
        .setCreatedDate(stability.getCreatedDate());
  }

  public static StabilityVo toIndicatorStabilityVo(IndicatorStability stability) {
    if (Objects.isNull(stability)) {
      return null;
    }
    return new StabilityVo().setId(stability.getId())
        .setTargetType(stability.getTargetType())
        .setTargetId(stability.getTargetId())
        .setTargetName(stability.getTargetName())
        .setThreads(stability.getThreads())
        .setDuration(stability.getDuration())
        .setArt(stability.getArt())
        .setPercentile(stability.getPercentile())
        .setTps(stability.getTps())
        .setErrorRate(stability.getErrorRate())
        .setCpu(stability.getCpu())
        .setMemory(stability.getMemory())
        .setDisk(stability.getDisk())
        .setNetwork(stability.getNetwork());
  }

  public static GenericSpecification<IndicatorStability> getSpecification(StabilityFindDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
    }
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("targetName")
        .rangeSearchFields("id", "createdDate")
        .build();
    return new GenericSpecification<>(filters);
  }

  public static Set<SearchCriteria> getSearchCriteria(StabilitySearchDto dto) {
    if (Objects.nonNull(dto.getAdminFlag()) && dto.getAdminFlag()) {
      dto.getFilters().add(SearchCriteria.equal("adminFlag", "true"));
    }
    // Build the final filters
    return new SearchCriteriaBuilder<>(dto)
        .matchSearchFields("targetName")
        .rangeSearchFields("id", "createdDate")
        .build();
  }
}




