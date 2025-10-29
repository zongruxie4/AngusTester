package cloud.xcan.angus.core.tester.interfaces.test.facade;

import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewAddDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewFindDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.dto.review.FuncReviewUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.review.FuncReviewDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;

public interface FuncReviewFacade {

  IdKey<Long, Object> add(FuncReviewAddDto dto);

  void update(FuncReviewUpdateDto dto);

  IdKey<Long, Object> replace(FuncReviewReplaceDto dto);

  void start(Long id);

  void end(Long id);

  void block(Long id);

  IdKey<Long, Object> clone(Long id);

  void reviewReset(HashSet<Long> ids);

  void reviewRestart(HashSet<Long> ids);

  void delete(Long id);

  FuncReviewDetailVo detail(Long id);

  PageResult<FuncReviewDetailVo> list(FuncReviewFindDto dto);

}
