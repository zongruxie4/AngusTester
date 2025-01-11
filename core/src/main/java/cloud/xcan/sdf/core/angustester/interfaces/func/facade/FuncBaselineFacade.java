package cloud.xcan.sdf.core.angustester.interfaces.func.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline.FuncBaselineUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.baseline.FuncBaselineVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface FuncBaselineFacade {

  IdKey<Long, Object> add(FuncBaselineAddDto dto);

  void update(FuncBaselineUpdateDto dto);

  IdKey<Long, Object> replace(FuncBaselineReplaceDto dto);

  void establish(Long id);

  void delete(Collection<Long> ids);

  FuncBaselineDetailVo detail(Long id);

  PageResult<FuncBaselineVo> list(FuncBaselineFindDto dto);

  PageResult<FuncBaselineVo> search(FuncBaselineSearchDto dto);

}
