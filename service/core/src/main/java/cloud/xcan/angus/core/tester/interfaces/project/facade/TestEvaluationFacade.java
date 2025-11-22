package cloud.xcan.angus.core.tester.interfaces.project.facade;

import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationAddDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationFindDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.dto.EvaluationUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.project.facade.vo.EvaluationDetailVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;

public interface TestEvaluationFacade {

  IdKey<Long, Object> add(EvaluationAddDto dto);

  void update(EvaluationUpdateDto dto);

  void generateResult(Long id);

  void delete(Long id);

  EvaluationDetailVo detail(Long id);

  PageResult<EvaluationDetailVo> list(EvaluationFindDto dto);
}

