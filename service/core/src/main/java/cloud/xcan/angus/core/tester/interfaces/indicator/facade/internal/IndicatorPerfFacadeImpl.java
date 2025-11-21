package cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler.toIndicatorPerfVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorPerfCmd;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.domain.config.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorPerfFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.PerfReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorPerfAssembler;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.PerfListVo;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.PerfVo;
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
    return toIndicatorPerfVo(indicatorPerfQuery.detailOrDefault(targetType, targetId));
  }

  @NameJoin
  @Override
  public PageResult<PerfListVo> list(PerfFindDto dto) {
    Page<IndicatorPerf> page = indicatorPerfQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, IndicatorPerfAssembler::toApisPerfListVo);
  }

}




