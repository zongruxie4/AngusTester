package cloud.xcan.angus.core.tester.interfaces.func.facade;

import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.angus.core.tester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface FuncBaselineFacade {

  IdKey<Long, Object> add(FuncBaselineAddDto dto);

  void update(FuncBaselineUpdateDto dto);

  IdKey<Long, Object> replace(FuncBaselineReplaceDto dto);

  void establish(Long id);

  void delete(Collection<Long> ids);

  FuncBaselineDetailVo detail(Long id);

  PageResult<FuncBaselineVo> list(FuncBaselineFindDto dto);

}
