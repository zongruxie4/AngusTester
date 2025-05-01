package cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal;

import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.getSearchCriteria;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.toIndicatorFuncVo;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.tester.indicator.vo.FuncVo;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.indicator.IndicatorFuncCmd;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncSearch;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.IndicatorFuncFacade;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler;
import cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class IndicatorFuncFacadeImpl implements IndicatorFuncFacade {

  @Resource
  private IndicatorFuncCmd indicatorFuncCmd;

  @Resource
  private IndicatorFuncQuery indicatorFuncQuery;

  @Resource
  private IndicatorFuncSearch indicatorFuncSearch;

  @Override
  public IdKey<Long, Object> add(FuncAddDto dto) {
    return indicatorFuncCmd.add(addDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void replace(FuncReplaceDto dto) {
    indicatorFuncCmd.replace(replaceDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void delete(Collection<Long> ids) {
    indicatorFuncCmd.delete(ids);
  }

  @Override
  public void deleteByTarget(CombinedTargetType targetType, Long targetId) {
    indicatorFuncCmd.deleteAllByTarget(List.of(targetId), targetType);
  }

  @Override
  public FuncVo detail(CombinedTargetType targetType, Long targetId) {
    return toIndicatorFuncVo(indicatorFuncQuery.find(targetId, targetType));
  }

  @Override
  public FuncVo detailOrDefault(CombinedTargetType targetType, Long targetId) {
    IndicatorFunc perf = indicatorFuncQuery.detailAndDefault(targetType, targetId);
    return toIndicatorFuncVo(perf);
  }

  @NameJoin
  @Override
  public PageResult<FuncListVo> list(FuncFindDto dto) {
    Page<IndicatorFunc> page = indicatorFuncQuery.list(getSpecification(dto),
        dto.tranPage(), IndicatorFunc.class);
    return buildVoPageResult(page, IndicatorFuncAssembler::toApisFuncListVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncListVo> search(FuncSearchDto dto) {
    Page<IndicatorFunc> page = indicatorFuncSearch.search(getSearchCriteria(dto),
        dto.tranPage(), IndicatorFunc.class);
    return buildVoPageResult(page, IndicatorFuncAssembler::toApisFuncListVo);
  }
}




