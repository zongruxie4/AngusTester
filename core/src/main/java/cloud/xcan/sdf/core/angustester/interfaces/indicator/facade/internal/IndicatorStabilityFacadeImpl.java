package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.addDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.getSearchCriteria;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.getSpecification;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.replaceDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler.toIndicatorStabilityVo;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.StabilityVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorStabilityCmd;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorStabilitySearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorStabilityFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilityReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.StabilitySearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorStabilityAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.StabilityListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
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
    return toIndicatorStabilityVo(indicatorStabilityQuery.detailAndDefault(targetType, tagetId));
  }

  @NameJoin
  @Override
  public PageResult<StabilityListVo> list(StabilityFindDto dto) {
    Page<IndicatorStability> indicatorStabilityPage = indicatorStabilityQuery
        .list(getSpecification(dto), dto.tranPage(), IndicatorStability.class);
    return buildVoPageResult(indicatorStabilityPage, IndicatorStabilityAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<StabilityListVo> search(StabilitySearchDto dto) {
    Page<IndicatorStability> indicatorStabilityPage = indicatorStabilitySearch
        .search(getSearchCriteria(dto), dto.tranPage(), IndicatorStability.class);
    return buildVoPageResult(indicatorStabilityPage, IndicatorStabilityAssembler::toListVo);
  }

}




