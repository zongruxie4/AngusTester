package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.application.query.func.FuncBaselineSearch;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaseline;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncBaselineFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncBaselineAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
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
