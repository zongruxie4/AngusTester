package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler.toAddDomain;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler.toReplaceDomain;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler.toUpdateDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncBaselineCmd;
import cloud.xcan.angus.core.tester.application.query.test.FuncBaselineQuery;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncBaselineFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncBaselineAssembler;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.baseline.FuncBaselineVo;
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
  private FuncBaselineCmd funcBaselineCmd;

  @Override
  public IdKey<Long, Object> add(FuncBaselineAddDto dto) {
    return funcBaselineCmd.add(toAddDomain(dto));
  }

  @Override
  public void update(FuncBaselineUpdateDto dto) {
    funcBaselineCmd.update(toUpdateDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(FuncBaselineReplaceDto dto) {
    return funcBaselineCmd.replace(toReplaceDomain(dto));
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
    return toDetailVo(funcBaselineQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<FuncBaselineVo> list(FuncBaselineFindDto dto) {
    Page<FuncBaselineInfo> page = funcBaselineQuery.find(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncBaselineAssembler::toListVo);
  }

}
