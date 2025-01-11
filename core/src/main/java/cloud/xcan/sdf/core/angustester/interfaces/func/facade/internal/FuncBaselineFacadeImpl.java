package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncBaselineCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncBaselineQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncBaselineSearch;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaseline;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncBaselineFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncBaselineAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncBaselineFacadeImpl implements FuncBaselineFacade {

  @Resource
  private FuncBaselineQuery funcBaselineQuery;

  @Resource
  private FuncBaselineSearch funcBaselineSearch;

  @Resource
  private FuncBaselineCmd funcBaselineCmd;

  @Override
  public IdKey<Long, Object> add(FuncBaselineAddDto dto) {
    FuncBaseline baseline = FuncBaselineAssembler.toAddDomain(dto);
    return funcBaselineCmd.add(baseline);
  }

  @Override
  public void update(FuncBaselineUpdateDto dto) {
    FuncBaseline baseline = FuncBaselineAssembler.toUpdateDomain(dto);
    funcBaselineCmd.update(baseline);
  }

  @Override
  public IdKey<Long, Object> replace(FuncBaselineReplaceDto dto) {
    FuncBaseline baseline = FuncBaselineAssembler.toReplaceDomain(dto);
    return funcBaselineCmd.replace(baseline);
  }

  @Override
  public void establish(Long id) {
    funcBaselineCmd.establish(id);
  }

  @Override
  public void delete(Collection<Long> ids) {
    funcBaselineCmd.delete(ids);
  }

  @NameJoin
  @Override
  public FuncBaselineDetailVo detail(Long id) {
    FuncBaseline baseline = funcBaselineQuery.detail(id);
    return FuncBaselineAssembler.toDetailVo(baseline);
  }

  @NameJoin
  @Override
  public PageResult<FuncBaselineVo> list(FuncBaselineFindDto dto) {
    Page<FuncBaselineInfo> page = funcBaselineQuery
        .find(FuncBaselineAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, FuncBaselineAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncBaselineVo> search(FuncBaselineSearchDto dto) {
    Page<FuncBaselineInfo> page = funcBaselineSearch
        .search(FuncBaselineAssembler.getSearchCriteria(dto), dto.tranPage(),
            FuncBaselineInfo.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncBaselineAssembler::toListVo);
  }
}
