package cloud.xcan.angus.core.tester.interfaces.func.facade;

import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanSearchDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.FuncPlanUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.FuncCaseListVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.plan.FuncPlanDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface FuncPlanFacade {

  IdKey<Long, Object> add(FuncPlanAddDto dto);

  void update(FuncPlanUpdateDto dto);

  IdKey<Long, Object> replace(FuncPlanReplaceDto dto);

  void start(Long id);

  void end(Long id);

  void block(Long id);

  IdKey<Long, Object> clone(Long id);

  void resultReset(HashSet<Long> ids);

  void reviewReset(HashSet<Long> ids);

  void delete(Long id);

  FuncPlanDetailVo detail(Long id);

  PageResult<FuncPlanDetailVo> list(FuncPlanFindDto dto);

  PageResult<FuncPlanDetailVo> search(FuncPlanSearchDto dto);

  List<FuncCaseListVo> notReviewed(Long id, Long moduleId, Long reviewId);

  List<FuncCaseListVo> notEstablishedBaseline(Long planId, Long moduleId, Long baselineId);
}
