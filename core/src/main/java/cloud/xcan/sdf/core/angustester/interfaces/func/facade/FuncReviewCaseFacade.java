package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.review.cases.FuncReviewCaseSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.review.FuncReviewCaseVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
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

  PageResult<FuncReviewCaseVo> search(FuncReviewCaseSearchDto dto);
}
