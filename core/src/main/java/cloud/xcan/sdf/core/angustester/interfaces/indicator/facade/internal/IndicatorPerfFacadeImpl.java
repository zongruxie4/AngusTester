package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.addDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.getSearchCriteria;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.getSpecification;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.replaceDtoToDomain;
import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.toIndicatorPerfVo;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.PerfVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorPerfSearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.PerfSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class IndicatorPerfFacadeImpl implements IndicatorPerfFacade {

  @Resource
  private IndicatorPerfCmd indicatorPerfCmd;

  @Resource
  private IndicatorPerfQuery indicatorPerfQuery;

  @Resource
  private IndicatorPerfSearch indicatorPerfSearch;

  @Override
  public IdKey<Long, Object> add(PerfAddDto dto) {
    return indicatorPerfCmd.add(addDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void replace(PerfReplaceDto dto) {
    indicatorPerfCmd.replace(replaceDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void delete(Collection<Long> ids) {
    indicatorPerfCmd.delete(ids);
  }

  @Override
  public void deleteByTarget(CombinedTargetType targetType, Long targetId) {
    indicatorPerfCmd.deleteAllByTarget(List.of(targetId), targetType);
  }

  @Override
  public PerfVo detail(CombinedTargetType targetType, Long targetId) {
    return toIndicatorPerfVo(indicatorPerfQuery.find(targetId, targetType));
  }

  @Override
  public PerfVo detailOrDefault(CombinedTargetType targetType, Long targetId) {
    return toIndicatorPerfVo(indicatorPerfQuery.detailAndDefault(targetType, targetId));
  }

  @NameJoin
  @Override
  public PageResult<PerfListVo> list(PerfFindDto dto) {
    Page<IndicatorPerf> indicatorPerfPage = indicatorPerfQuery
        .list(getSpecification(dto), dto.tranPage(), IndicatorPerf.class);
    return buildVoPageResult(indicatorPerfPage, IndicatorPerfAssembler::toApisPerfListVo);
  }

  @NameJoin
  @Override
  public PageResult<PerfListVo> search(PerfSearchDto dto) {
    Page<IndicatorPerf> indicatorPerfPage = indicatorPerfSearch.search(
        getSearchCriteria(dto), dto.tranPage(), IndicatorPerf.class);
    return buildVoPageResult(indicatorPerfPage, IndicatorPerfAssembler::toApisPerfListVo);
  }
}




