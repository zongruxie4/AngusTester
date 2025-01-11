package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCaseCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewCaseSearch;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncReviewCaseFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncReviewCaseAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncReviewCaseFacadeImpl implements FuncReviewCaseFacade {

  @Resource
  private FuncReviewCaseCmd funcReviewCaseCmd;

  @Resource
  private FuncReviewCaseQuery funcReviewCaseQuery;

  @Resource
  private FuncReviewCaseSearch funcReviewCaseSearch;

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
    FuncReviewCaseDetailVo detailVo = FuncReviewCaseAssembler.toDetail(reviewCase);
    detailVo.setLatestCase(funcCaseFacade.detail(reviewCase.getCaseId()));
    return detailVo;
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewCaseVo> list(FuncReviewCaseFindDto dto) {
    Page<FuncReviewCase> page = funcReviewCaseQuery
        .list(FuncReviewCaseAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, FuncReviewCaseAssembler::toListVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewCaseVo> search(FuncReviewCaseSearchDto dto) {
    Page<FuncReviewCase> page = funcReviewCaseSearch
        .search(FuncReviewCaseAssembler.getSearchCriteria(dto), dto.tranPage(),
            FuncReviewCase.class, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncReviewCaseAssembler::toListVo);
  }
}
