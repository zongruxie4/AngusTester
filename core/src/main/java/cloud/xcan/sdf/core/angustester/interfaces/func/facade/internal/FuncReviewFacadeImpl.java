package cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal;

import static cloud.xcan.sdf.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.sdf.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.application.cmd.func.FuncReviewCmd;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewQuery;
import cloud.xcan.sdf.core.angustester.application.query.func.FuncReviewSearch;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.FuncReviewFacade;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.internal.assembler.FuncReviewAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewDetailVo;
import cloud.xcan.sdf.core.biz.NameJoin;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import javax.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncReviewFacadeImpl implements FuncReviewFacade {

  @Resource
  private FuncReviewCmd funcReviewCmd;

  @Resource
  private FuncReviewQuery funcReviewQuery;

  @Resource
  private FuncReviewSearch funcReviewSearch;

  @Override
  public IdKey<Long, Object> add(FuncReviewAddDto dto) {
    return funcReviewCmd.add(FuncReviewAssembler.addDtoToDomain(dto));
  }

  @Override
  public void update(FuncReviewUpdateDto dto) {
    funcReviewCmd.update(FuncReviewAssembler.updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(FuncReviewReplaceDto dto) {
    return funcReviewCmd.replace(FuncReviewAssembler.replaceDtoToDomain(dto));
  }

  @Override
  public void start(Long id) {
    funcReviewCmd.start(id);
  }

  @Override
  public void end(Long id) {
    funcReviewCmd.end(id);
  }

  @Override
  public void block(Long id) {
    funcReviewCmd.block(id);
  }

  @Override
  public IdKey<Long, Object> clone(Long id) {
    return funcReviewCmd.clone(id);
  }

  @Override
  public void reviewReset(HashSet<Long> ids) {
    funcReviewCmd.reviewReset(ids, true);
  }

  @Override
  public void reviewRestart(HashSet<Long> ids) {
    funcReviewCmd.reviewReset(ids, false);
  }

  @Override
  public void delete(Long id) {
    funcReviewCmd.delete(id);
  }

  @NameJoin
  @Override
  public FuncReviewDetailVo detail(Long id) {
    FuncReview funcReview = funcReviewQuery.detail(id);
    return FuncReviewAssembler.toDetailVo(funcReview);
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewDetailVo> list(FuncReviewFindDto dto) {
    Page<FuncReview> page = funcReviewQuery
        .find(FuncReviewAssembler.getSpecification(dto), dto.tranPage());
    return buildVoPageResult(page, FuncReviewAssembler::toDetailVo);
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewDetailVo> search(FuncReviewSearchDto dto) {
    Page<FuncReview> page = funcReviewSearch
        .search(FuncReviewAssembler.getSearchCriteria(dto), dto.tranPage(), FuncReview.class,
            getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncReviewAssembler::toDetailVo);
  }

}
