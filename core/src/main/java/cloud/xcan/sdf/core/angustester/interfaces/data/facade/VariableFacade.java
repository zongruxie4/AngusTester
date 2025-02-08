package cloud.xcan.sdf.core.angustester.interfaces.data.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable.VariableValuePreviewDto;
import cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.variable.VariableDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface VariableFacade {

  IdKey<Long, Object> add(VariableAddDto dto);

  void update(VariableUpdateDto dto);

  IdKey<Long, Object> replace(VariableReplaceDto dto);

  List<IdKey<Long, Object>> clone(HashSet<Long> ids);

  List<IdKey<Long, Object>> imports(VariableImportDto dto);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

  VariableDetailVo detail(Long id);

  String valuePreview(VariableValuePreviewDto dto);

  PageResult<VariableDetailVo> list(VariableFindDto dto);

  PageResult<VariableDetailVo> search(VariableSearchDto dto);

  ResponseEntity<Resource> export(VariableExportDto dto, HttpServletResponse response);

}
