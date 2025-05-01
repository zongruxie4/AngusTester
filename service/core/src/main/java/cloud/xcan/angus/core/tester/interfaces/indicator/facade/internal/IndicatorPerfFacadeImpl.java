package cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.toIndicatorPerfVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.tester.indicator.vo.PerfVo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfSearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfSearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
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
    Page<IndicatorPerf> page = indicatorPerfQuery
        .list(getSpecification(dto), dto.tranPage(), IndicatorPerf.class);
    return buildVoPageResult(page, IndicatorPerfAssembler::toApisPerfListVo);
  }

  @NameJoin
  @Override
  public PageResult<PerfListVo> search(PerfSearchDto dto) {
    Page<IndicatorPerf> page = indicatorPerfSearch.search(
        getSearchCriteria(dto), dto.tranPage(), IndicatorPerf.class);
    return buildVoPageResult(page, IndicatorPerfAssembler::toApisPerfListVo);
  }
}




