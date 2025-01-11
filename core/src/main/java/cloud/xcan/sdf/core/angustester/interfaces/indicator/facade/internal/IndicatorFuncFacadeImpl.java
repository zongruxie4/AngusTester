package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal;

import static cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler.toIndicatorFuncVo;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.api.angustester.indicator.vo.FuncVo;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.core.angustester.application.cmd.indicator.IndicatorFuncCmd;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.sdf.core.angustester.application.query.indicator.IndicatorFuncSearch;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorFunc;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.IndicatorFuncFacade;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto.FuncSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.internal.assembler.IndicatorFuncAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo.FuncListVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import javax.annotation.Resource;
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
    return indicatorFuncCmd.add(IndicatorFuncAssembler.addDtoToDomain(dto, dto.getTargetType()));
  }

  @Override
  public void replace(FuncReplaceDto dto) {
    indicatorFuncCmd.replace(IndicatorFuncAssembler.replaceDtoToDomain(dto, dto.getTargetType()));
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
    Page<IndicatorFunc> indicatorFuncPage = indicatorFuncQuery
        .list(IndicatorFuncAssembler.getSpecification(dto), dto.tranPage(), IndicatorFunc.class);
    return buildVoPageResult(indicatorFuncPage, IndicatorFuncAssembler::toApisFuncListVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncListVo> search(FuncSearchDto dto) {
    Page<IndicatorFunc> indicatorFuncPage = indicatorFuncSearch.search(
        IndicatorFuncAssembler.getSearchCriteria(dto), dto.tranPage(), IndicatorFunc.class);
    return buildVoPageResult(indicatorFuncPage, IndicatorFuncAssembler::toApisFuncListVo);
  }
}




