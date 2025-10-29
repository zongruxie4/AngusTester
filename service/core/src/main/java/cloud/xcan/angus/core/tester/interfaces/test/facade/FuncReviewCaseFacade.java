package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public interface FuncReviewCaseFacade {

  List<IdKey<Long, Object>> add(FuncReviewCaseAddDto dto);

  void review(List<FuncReviewCaseDto> dto);

  void reviewReset(HashSet<Long> ids);

  void reviewStart(HashSet<Long> ids);

  void delete(Collection<Long> ids);

  FuncReviewCaseDetailVo detail(Long id);

  PageResult<FuncReviewCaseVo> list(FuncReviewCaseFindDto dto);
}
