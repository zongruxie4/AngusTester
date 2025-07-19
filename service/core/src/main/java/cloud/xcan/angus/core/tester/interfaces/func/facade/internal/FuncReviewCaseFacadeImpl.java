package cloud.xcan.angus.core.tester.interfaces.func.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncReviewCaseAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncReviewCaseAssembler.toDetail;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.angus.core.tester.application.query.func.FuncReviewCaseQuery;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.FuncReviewCaseFacade;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.internal.assembler.FuncReviewCaseAssembler;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncReviewCaseFacadeImpl implements FuncReviewCaseFacade {

  @Resource
  private FuncReviewCaseCmd funcReviewCaseCmd;

  @Resource
  private FuncReviewCaseQuery funcReviewCaseQuery;

  @Resource
  private FuncCaseFacade funcCaseFacade;

  @Override
  public List<IdKey<Long, Object>> add(FuncReviewCaseAddDto dto) {
    return funcReviewCaseCmd.add(dto.getReviewId(), dto.getCaseIds());
  }

  @Override
  public void review(List<FuncReviewCaseDto> dto) {
    List<FuncReviewCase> reviewCases = dto.stream()
        .map(FuncReviewCaseAssembler::reviewDtoToDomain).collect(Collectors.toList());
    funcReviewCaseCmd.review(reviewCases);
  }

  @Override
  public void reviewReset(HashSet<Long> ids) {
    funcReviewCaseCmd.reviewReset(ids, true);
  }

  @Override
  public void reviewStart(HashSet<Long> ids) {
    funcReviewCaseCmd.reviewReset(ids, false);
  }

  @Override
  public void delete(Collection<Long> ids) {
    funcReviewCaseCmd.delete(ids);
  }

  @NameJoin
  @Override
  public FuncReviewCaseDetailVo detail(Long id) {
    FuncReviewCase reviewCase = funcReviewCaseQuery.detail(id);
    FuncReviewCaseDetailVo detailVo = toDetail(reviewCase);
    detailVo.setLatestCase(funcCaseFacade.detail(reviewCase.getCaseId()));
    return detailVo;
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewCaseVo> list(FuncReviewCaseFindDto dto) {
    Page<FuncReviewCase> page = funcReviewCaseQuery.list(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncReviewCaseAssembler::toListVo);
  }

}
