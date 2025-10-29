package cloud.xcan.angus.core.tester.interfaces.test.facade.internal;

import static cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder.getMatchSearchFields;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler.addDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler.getSpecification;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler.replaceDtoToDomain;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler.toDetailVo;
import static cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler.updateDtoToDomain;
import static cloud.xcan.angus.core.utils.CoreUtils.buildVoPageResult;

import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncReviewCmd;
import cloud.xcan.angus.core.tester.application.query.test.FuncReviewQuery;
import cloud.xcan.angus.core.tester.domain.test.review.FuncReview;
import cloud.xcan.angus.core.tester.interfaces.test.facade.FuncReviewFacade;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.internal.assembler.FuncReviewAssembler;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.review.FuncReviewDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import jakarta.annotation.Resource;
import java.util.HashSet;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class FuncReviewFacadeImpl implements FuncReviewFacade {

  @Resource
  private FuncReviewCmd funcReviewCmd;

  @Resource
  private FuncReviewQuery funcReviewQuery;

  @Override
  public IdKey<Long, Object> add(FuncReviewAddDto dto) {
    return funcReviewCmd.add(addDtoToDomain(dto));
  }

  @Override
  public void update(FuncReviewUpdateDto dto) {
    funcReviewCmd.update(updateDtoToDomain(dto));
  }

  @Override
  public IdKey<Long, Object> replace(FuncReviewReplaceDto dto) {
    return funcReviewCmd.replace(replaceDtoToDomain(dto));
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
    return toDetailVo(funcReviewQuery.detail(id));
  }

  @NameJoin
  @Override
  public PageResult<FuncReviewDetailVo> list(FuncReviewFindDto dto) {
    Page<FuncReview> page = funcReviewQuery.find(getSpecification(dto), dto.tranPage(),
        dto.fullTextSearch, getMatchSearchFields(dto.getClass()));
    return buildVoPageResult(page, FuncReviewAssembler::toDetailVo);
  }

}
