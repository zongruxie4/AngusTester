package cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.toIndicatorStabilityVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilitySearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.StabilityVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class IndicatorStabilityFacadeImpl implements IndicatorStabilityFacade {

  @Resource
  private IndicatorStabilityCmd indicatorStabilityCmd;

  @Resource
  private IndicatorStabilityQuery indicatorStabilityQuery;

  @Resource
  private IndicatorStabilitySearch indicatorStabilitySearch;

  @Override
  public IdKey<Long, Object> add(StabilityAddDto dto) {
    return indicatorStabilityCmd.add(addDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void replace(StabilityReplaceDto dto) {
    indicatorStabilityCmd.replace(replaceDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void delete(Collection<Long> ids) {
    indicatorStabilityCmd.delete(ids);
  }

  @Override
  public void deleteByTarget(CombinedTargetType targetType, Long targetId) {
    indicatorStabilityCmd.deleteAllByTarget(List.of(targetId), targetType);
  }

  @Override
  public StabilityVo detail(CombinedTargetType targetType, Long targetId) {
    return toIndicatorStabilityVo(indicatorStabilityQuery.find(targetId, targetType));
  }

  @Override
  public StabilityVo detailOrDefault(CombinedTargetType targetType, Long tagetId) {
    return toIndicatorStabilityVo(indicatorStabilityQuery.detailOrDefault(targetType, tagetId));
  }

  @NameJoin
  @Override
  public PageResult<StabilityListVo> list(StabilityFindDto dto) {
    Page<IndicatorStability> page = indicatorStabilityQuery
        .list(getSpecification(dto), dto.tranPage(), IndicatorStability.class);
    return buildVoPageResult(page, IndicatorStabilityAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<StabilityListVo> search(StabilitySearchDto dto) {
    Page<IndicatorStability> page = indicatorStabilitySearch
        .search(getSearchCriteria(dto), dto.tranPage(), IndicatorStability.class);
    return buildVoPageResult(page, IndicatorStabilityAssembler::toListVo);
  }

}




